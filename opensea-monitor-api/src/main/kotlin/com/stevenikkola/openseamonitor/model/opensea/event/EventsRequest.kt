package com.stevenikkola.openseamonitor.model.opensea.event

import com.stevenikkola.openseamonitor.service.opensea.OpenseaEventService.Companion.DEFAULT_MAX_PAGE_LIMIT

data class EventsRequest(
    val contractAddress: String,
    val eventType: EventType,
    val onlyOpenseaAuctions: Boolean? = true,
    val auctionType: AuctionType? = AuctionType.ENGLISH,
    val offset: Int? = 0,
    val limit: Int? = DEFAULT_MAX_PAGE_LIMIT,
    val occurredBeforeEpochSeconds: Long?,
    val occurredAfterEpochSeconds: Long?,
    val maxEventsToGather: Int? = 200,
    val discordWebTokens: List<String>? = null
)

enum class AuctionType(val value: String) {
    ENGLISH("english"),
    DUTCH("dutch"),
    MIN_PRICE("min-price")
}

enum class EventType(val value: String) {
    CREATED("created"),
    SUCCESSFUL("successful"),
}
