package com.stevenikkola.openseamonitor.model.opensea.event

import com.fasterxml.jackson.annotation.JsonProperty
import com.stevenikkola.openseamonitor.domain.OpenseaEvent

data class EventsResponse(
    @JsonProperty("asset_events")
    val events: List<OpenseaEvent> = emptyList()
)