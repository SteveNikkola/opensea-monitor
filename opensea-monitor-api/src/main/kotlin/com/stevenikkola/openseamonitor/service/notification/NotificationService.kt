package com.stevenikkola.openseamonitor.service.notification

import com.stevenikkola.openseamonitor.domain.OpenseaEvent

interface NotificationService {
    suspend fun sendNotification(
        openseaEvents: List<OpenseaEvent>,
        webhookToken: String
    )
}
