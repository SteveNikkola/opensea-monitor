package com.stevenikkola.openseamonitor.service.opensea

import com.stevenikkola.openseamonitor.client.OpenSeaClient
import com.stevenikkola.openseamonitor.domain.OpenseaEvent
import com.stevenikkola.openseamonitor.model.opensea.event.EventType
import com.stevenikkola.openseamonitor.model.opensea.event.EventsRequest
import com.stevenikkola.openseamonitor.repository.OpenseaEventRepository
import com.stevenikkola.openseamonitor.service.notification.NotificationService
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

@Service
class OpenseaEventService(
    private val openSeaClient: OpenSeaClient,
    private val openseaEventRepository: OpenseaEventRepository,
    private val notificationService: NotificationService
) {

    companion object {
        const val POPULATE_BASE_DB_EVENTS_QTY = 1
        const val DEFAULT_MAX_EVENTS = 200
        const val DEFAULT_MAX_PAGE_LIMIT = 50
    }

    private val logger = KLogging().logger()

    fun retrieveEvents(eventsRequest: EventsRequest): List<OpenseaEvent>? {

        val mostRecents =
            openseaEventRepository.findMostRecentEventForCollection(
                event = eventsRequest.eventType,
                contractAddress = eventsRequest.contractAddress
            )

        val mostRecent = mostRecents!!.firstOrNull()

        var occurredAfter = getLastCreatedDateEpochSeconds(mostRecent)
        var occuredBefore = getCurrentEpochSeconds()

        logger.info { "Using Epoch seconds occured after of : $occurredAfter" }

        var pagingComplete = false

        var lastEvent: OpenseaEvent? = null
        var lastEventId: String? = null
        val eventsToProcess: MutableList<OpenseaEvent> = mutableListOf()

        val defaultMaxEvents: Int = eventsRequest.maxEventsToGather ?: DEFAULT_MAX_EVENTS
        val maxPages: Int = defaultMaxEvents.div(DEFAULT_MAX_PAGE_LIMIT)
        var pages = 0
        runBlocking {
            do {
                logger.info { "Calling Opensea with epoch seconds occuredAfter: $occurredAfter and occuredBefore: $occuredBefore for ${eventsRequest.contractAddress}" }
                val nextEvents = getNextEventsFromOpensea(
                    eventsRequest.copy(
                        occurredAfterEpochSeconds = occurredAfter,
                        occurredBeforeEpochSeconds = occuredBefore
                    )
                )

                val idExists = nextEvents.any { it.id == mostRecent?.id }
                logger.info { "Id of ${mostRecent?.id} exists: $idExists" }

                if (nextEvents.isEmpty()) {
                    logger.info { "No additional events found, done paging" }
                    pagingComplete = true
                    continue
                }

                val eventsToAdd = nextEvents.filter { nextEvent -> !eventsToProcess.map { it.id }.contains(nextEvent.id) }
                eventsToProcess.addAll(eventsToAdd)

                if (eventsToProcess.size > defaultMaxEvents) {
                    logger.info { "eventsToProcess size of ${eventsToProcess.size} exceeds max allowed to gather of $defaultMaxEvents, done paging" }
                    pagingComplete = true
                    continue
                }

                if (nextEvents.any { it.id == mostRecent?.id }) {
                    logger.info { "Found most recent id of ${mostRecent?.id} in latest page attempt, done paging." }
                    pagingComplete = true
                    continue
                }

                if (nextEvents.size < (eventsRequest.limit ?: 0)) {
                    logger.info { "Events returned size of ${nextEvents.size} is smaller than requested page size of ${eventsRequest.limit}, done paging." }
                    pagingComplete = true
                    continue
                }

                lastEvent = nextEvents.minByOrNull { it.createdDate }

                logger.info { "Last event in this group id: ${lastEvent!!.id} createdDate: ${lastEvent!!.createdDate}" }
                lastEventId = lastEvent?.id
                occuredBefore = getLastCreatedDateEpochSeconds(lastEvent)
                pages += 1
                delay(2000)
            } while (!pagingComplete && (pages < maxPages))

            eventsRequest.discordWebTokens?.forEach {
                notificationService.sendNotification(
                    openseaEvents = eventsToProcess,
                    webhookToken = it
                )
            }

        }

        openseaEventRepository.saveAll(eventsToProcess)

        return eventsToProcess

    }

    private fun getLastCreatedDateEpochSeconds(openseaEvent: OpenseaEvent?): Long {

        val occuredAfterZoneDateTime = if (openseaEvent != null) {
            logger.info { "Found most recent existing record with id ${openseaEvent.id} and createdDate of ${openseaEvent.createdDate}" }
            ZonedDateTime.of(openseaEvent.createdDate, ZoneId.of("UTC"))
        } else {
            logger.info { "No existing events found, looking for events since beginning" }
            ZonedDateTime.of(LocalDateTime.of(1970, 1, 1, 0, 0), ZoneId.systemDefault())
        }

        return TimeUnit.MILLISECONDS.toSeconds(occuredAfterZoneDateTime.toInstant().toEpochMilli())
    }

    fun populateDbWithBaseEvents(eventsRequest: EventsRequest): List<OpenseaEvent> {

        logger.info { "Populating database with most recent $POPULATE_BASE_DB_EVENTS_QTY events for contract address ${eventsRequest.contractAddress}" }

        logger.info { "First getting recently listed events" }
        val baseListedEvents = getNextEventsFromOpensea(
            eventsRequest.copy(
                offset = 0,
                limit = POPULATE_BASE_DB_EVENTS_QTY,
                occurredAfterEpochSeconds = 0,
                eventType = EventType.CREATED
            )
        )

        openseaEventRepository.saveAll(baseListedEvents)

        logger.info { "Now getting recently sold events" }
        val baseSoldEvents = getNextEventsFromOpensea(
            eventsRequest.copy(
                offset = 0,
                limit = POPULATE_BASE_DB_EVENTS_QTY,
                occurredAfterEpochSeconds = 0,
                eventType = EventType.SUCCESSFUL
            )
        )

        openseaEventRepository.saveAll(baseSoldEvents)

        return baseListedEvents
    }

    private fun getNextEventsFromOpensea(eventsRequest: EventsRequest): List<OpenseaEvent> {
        val eventsResponse = openSeaClient.retrieveEvents(eventsRequest)

        val events: List<OpenseaEvent> = eventsResponse?.events ?: emptyList()

        // map the IDs of the events we found
        val eventIds = events.map { it.id }

        // find if any of these event IDs already exist in our database
        val existingEvents: List<OpenseaEvent> = openseaEventRepository.findByIdIn(eventIds) ?: emptyList()

        // get a list of IDs that already exist
        val existingIds: List<String> = existingEvents.map { it.id }

        return events.filter { !existingIds.contains(it.id) }
    }

    private fun getCurrentEpochSeconds(): Long {
        return TimeUnit.MILLISECONDS.toSeconds(
            ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
    }
}