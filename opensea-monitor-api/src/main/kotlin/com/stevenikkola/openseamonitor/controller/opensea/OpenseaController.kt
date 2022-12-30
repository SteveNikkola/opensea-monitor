package com.stevenikkola.openseamonitor.controller.opensea

import com.stevenikkola.openseamonitor.domain.OpenseaEvent
import com.stevenikkola.openseamonitor.model.opensea.event.EventsRequest
import com.stevenikkola.openseamonitor.service.opensea.OpenseaEventService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/opensea-monitor/monitor/v1")
class OpenseaController(
    private val openseaEventService: OpenseaEventService
) {

    @PostMapping("retrieve_events")
    fun retrieveEvents(
        @RequestBody eventsRequest: EventsRequest
    ): List<OpenseaEvent>? {
        return openseaEventService.retrieveEvents(eventsRequest)
    }

    @PostMapping("populate_base_events_in_db")
    fun populateBaseEventsInDb(
        @RequestBody eventsRequest: EventsRequest
    ): List<OpenseaEvent>? {
        return openseaEventService.populateDbWithBaseEvents(eventsRequest)
    }
}