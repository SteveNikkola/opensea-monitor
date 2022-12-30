package com.stevenikkola.openseamonitor.repository

import com.stevenikkola.openseamonitor.domain.OpenseaEvent
import org.springframework.data.repository.CrudRepository

interface OpenseaEventRepository: CrudRepository<OpenseaEvent, String>, OpenseaEventRepositoryCustom {
    fun findByIdIn(ids: List<String>): List<OpenseaEvent>?
}