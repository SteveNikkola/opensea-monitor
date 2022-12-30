package com.stevenikkola.openseamonitor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@SpringBootApplication
@EnableMongoRepositories
class OpenseaMonitorApplication

fun main(args: Array<String>) {
	runApplication<OpenseaMonitorApplication>(*args)
}
