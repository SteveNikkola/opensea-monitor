package com.stevenikkola.openseamonitor.repository

import com.stevenikkola.openseamonitor.domain.OpenseaEvent
import com.stevenikkola.openseamonitor.model.opensea.event.EventType
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

class OpenseaEventRepositoryCustomImpl(
    private val mongoTemplate: MongoTemplate
) : OpenseaEventRepositoryCustom {

    override fun findMostRecentEventForCollection(eventType: EventType, contractAddress: String): List<OpenseaEvent>? {

        val eventTypeCriteria = Criteria.where("eventType").`is`(eventType.value)
        val collectionCriteria = Criteria.where("contractAddress").`is`(contractAddress)

        val query = Query(Criteria().andOperator(eventTypeCriteria, collectionCriteria))

        query.with(Sort.by(Sort.Direction.DESC, "createdDate"))

        return mongoTemplate.find(query, OpenseaEvent::class.java)
    }
}