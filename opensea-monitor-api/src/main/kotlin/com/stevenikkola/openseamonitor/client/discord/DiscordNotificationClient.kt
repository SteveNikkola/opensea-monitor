package com.stevenikkola.openseamonitor.client.discord

import com.stevenikkola.openseamonitor.model.discord.DiscordNotification
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class DiscordNotificationClient(
    private val restTemplate: RestTemplate,
    @Value("\${notification.discord.base-url}") private val baseUrl: String
) {

    fun notify(discordNotification: DiscordNotification, discordWebhookToken: String): ResponseEntity<Void> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val entity = HttpEntity(discordNotification, headers)
        return restTemplate.postForEntity(
            "$baseUrl/$discordWebhookToken",
            entity,
            Void::class.java
        )
    }
}