package com.stevenikkola.openseamonitor.client

import com.stevenikkola.openseamonitor.exception.OpenSeaMonitorException
import com.stevenikkola.openseamonitor.model.opensea.event.EventsRequest
import com.stevenikkola.openseamonitor.model.opensea.event.EventsResponse
import com.stevenikkola.openseamonitor.model.opensea.orderbook.OrderbookQuery
import com.stevenikkola.openseamonitor.model.opensea.orderbook.OrderbookResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class OpenSeaClient(
    private val restTemplate: RestTemplate,
    @Value("\${client.open-sea.api-base-url}") private val baseUrl: String,
    @Value("\${client.open-sea.orderbook.orderbook-base-path}") private val orderbookBasePath: String,
    @Value("\${client.open-sea.orderbook.orders-path}") private val ordersPath: String,
    @Value("\${client.open-sea.api-base-path}") private val apiBasePath: String,
    @Value("\${client.open-sea.events-path}") private val eventsPath: String,
    @Value("\${client.open-sea.assets-path}") private val assetsPath: String,
    ) {

    private val logger = KotlinLogging.logger {}

    companion object {
        const val CONTRACT_ADDRESS = "asset_contract_address"
        const val COLLECTION = "collection"
        const val EVENT_TYPE = "event_type"
        const val ONLY_OPENSEA = "only_opensea"
        const val AUCTION_TYPE = "auction_type"
        const val OFFSET = "offset"
        const val LIMIT = "limit"
        const val OCCURED_BEFORE = "occurred_before"
        const val OCCURED_AFTER = "occurred_after"
        const val TOKEN_IDS = "token_ids"
    }

    fun retrieveOrders(orderbookQuery: OrderbookQuery): OrderbookResponse? {

        val url = "$baseUrl/$orderbookBasePath/$ordersPath"

        val builder: UriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(url)

        return try {
            restTemplate.getForObject(builder.toUriString(), OrderbookResponse::class.java)
        } catch (e: Exception) {
            throw OpenSeaMonitorException("Error while calling $url to retrieveOrders: ${e.message}").also {
                logger.error { it.message }
            }
        }
    }

    fun retrieveEvents(eventsRequest: EventsRequest): EventsResponse? {

        val url = "$baseUrl/$apiBasePath/$eventsPath"

        val builder: UriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam(CONTRACT_ADDRESS, eventsRequest.contractAddress)
            .queryParam(ONLY_OPENSEA, eventsRequest.onlyOpenseaAuctions)
            .queryParam(OFFSET, 0)
            .queryParam(OCCURED_AFTER, eventsRequest.occurredAfterEpochSeconds)
            .queryParam(EVENT_TYPE, eventsRequest.eventType.value)
            .queryParam(LIMIT, eventsRequest.limit)

        eventsRequest.occurredBeforeEpochSeconds?.let {
            builder.queryParam(OCCURED_BEFORE, eventsRequest.occurredBeforeEpochSeconds)
        }

        logger.info { "Calling ${builder.toUriString()}" }

        return try {
            restTemplate.getForObject(builder.toUriString(), EventsResponse::class.java)
        } catch (e: Exception) {
            throw OpenSeaMonitorException("Error while calling $url to retrieveEvents: ${e.message}").also {
                logger.error { it.message }
            }
        }
    }
}