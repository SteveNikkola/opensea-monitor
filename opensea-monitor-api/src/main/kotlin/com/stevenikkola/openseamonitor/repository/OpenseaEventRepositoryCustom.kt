package com.stevenikkola.openseamonitor.repository

import com.stevenikkola.openseamonitor.domain.OpenseaEvent
import com.stevenikkola.openseamonitor.model.opensea.event.EventType

interface OpenseaEventRepositoryCustom {

    fun findMostRecentEventForCollection(event: EventType, contractAddress: String): List<OpenseaEvent>?
}