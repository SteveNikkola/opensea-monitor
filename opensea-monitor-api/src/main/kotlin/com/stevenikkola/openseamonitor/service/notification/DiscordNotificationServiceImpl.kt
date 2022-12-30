package com.stevenikkola.openseamonitor.service.notification

import com.stevenikkola.openseamonitor.domain.OpenseaEvent
import com.stevenikkola.openseamonitor.exception.OpenSeaMonitorException
import com.stevenikkola.openseamonitor.client.discord.DiscordNotificationClient
import com.stevenikkola.openseamonitor.model.opensea.event.EventType
import com.stevenikkola.openseamonitor.model.discord.DiscordAuthor
import com.stevenikkola.openseamonitor.model.discord.DiscordEmbed
import com.stevenikkola.openseamonitor.model.discord.DiscordEmbedField
import com.stevenikkola.openseamonitor.model.discord.DiscordEmbedFooter
import com.stevenikkola.openseamonitor.model.discord.DiscordEmbedThumbnail
import com.stevenikkola.openseamonitor.model.discord.DiscordNotification
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.text.DecimalFormat
import java.time.LocalDateTime

const val DEFAULT_PRODUCT_MONITOR_FOOTER_ICON =
    "https://emojipedia-us.s3.dualstack.us-west-1.amazonaws.com/thumbs/120/apple/285/rocket_1f680.png"

@Service
class DiscordNotificationServiceImpl(
    private val discordNotificationClient: DiscordNotificationClient,
    @Value("\${open-sea.base-url}") private val openseaBaseUrl: String,
    ) : NotificationService {

    companion object {
        const val X_RATE_LIMIT_REMAINING_HEADER = "x-ratelimit-remaining"
        const val X_RATE_LIMIT_RESET_HEADER = "x-ratelimit-reset"
        const val RETRY_AFTER_HEADER = "retry-after"
        const val TOO_MANY_REQUESTS_ERROR_CODE = 429
        const val ETH_SYMBOL = "ETH"
        const val SEND_DELAY_MS = 250L
    }

    private val logger = KotlinLogging.logger {}

    override suspend fun sendNotification(
        openseaEvents: List<OpenseaEvent>,
        webhookToken: String
    ) {

        logger.info { "Sending ${openseaEvents.size} Notifications" }

        openseaEvents.sortedByDescending { it.createdDate }.reversed().forEach { openseaEvent ->

            val embedFields: MutableList<DiscordEmbedField> = mutableListOf()

            val seller = openseaEvent.seller.user?.username ?: formatWalletAddress(openseaEvent.seller.address)
            embedFields.add(
                DiscordEmbedField(
                    name = "Seller",
                    value = "[$seller]($openseaBaseUrl/${openseaEvent.seller.address})",
                    inline = true
                ),
            )

            val isSold = openseaEvent.buyer?.address != null

            if (isSold) {
                val buyer = openseaEvent.buyer?.user?.username ?: formatWalletAddress(openseaEvent.buyer!!.address)
                embedFields.add(
                    DiscordEmbedField(
                    name = "Buyer",
                    value = "[$buyer]($openseaBaseUrl/${openseaEvent.buyer!!.address})",
                    inline = true
                )
                )
            }

            var price = if (isSold) {
                openseaEvent.total_price?.toDouble()
            } else {
                openseaEvent.startingPrice?.toDouble()
            }

            if (openseaEvent.payment_token.symbol == ETH_SYMBOL) {
                price = formatPrice(price)

                embedFields.add(
                    DiscordEmbedField(
                        name = "USD Price",
                        value = "$${DecimalFormat("#.00").format(price?.times(openseaEvent.payment_token.usdPrice.toDouble()))}",
                        inline = true
                    )
                )
            }

            val notificationType = getNoticationType(EventType.valueOf(openseaEvent.eventType.uppercase()))

            val title = openseaEvent.asset.name ?: "${openseaEvent.contractAddress} #${openseaEvent.asset.tokenId}"
            val discordNotification = DiscordNotification(
                embeds = listOf(
                    DiscordEmbed(
                        author = DiscordAuthor(
                            name = openseaEvent.contractAddress,
                            iconUrl = openseaEvent.asset.collection.imageUrl
                        ),
                        title = "$title $notificationType for $price ${openseaEvent.payment_token.symbol}",
                        thumbnail = DiscordEmbedThumbnail(url = openseaEvent.asset.imageThumbnailUrl),
                        url = openseaEvent.asset.permalink,
                        fields = embedFields,
                        footer = DiscordEmbedFooter(
                            text = "crypto-monitor || ${LocalDateTime.now()}",
                            iconUrl = DEFAULT_PRODUCT_MONITOR_FOOTER_ICON
                        )
                    )
                )
            )

            logger.info { "Sending Discord notification for ${openseaEvent.contractAddress} ${openseaEvent.asset.tokenId} $notificationType for $price" }

            try {
                val response = notify(discordNotification, webhookToken)
                handleRateLimit(response)
            } catch (e: Exception) {
                if (e is HttpClientErrorException) {
                    val httpStatusCode: Int = e.statusCode.value()

                    if (httpStatusCode == TOO_MANY_REQUESTS_ERROR_CODE) {
                        val retryAfter: Long? = e.responseHeaders?.get(RETRY_AFTER_HEADER)?.firstOrNull()?.toLong()

                        logger.warn { "Http error $TOO_MANY_REQUESTS_ERROR_CODE too many requests when sending discord notification for ${discordNotification.embeds.firstOrNull()?.title}. Sleeping for ${retryAfter}ms and trying again" }

                        if (retryAfter != null && retryAfter > 0) {
                            Thread.sleep(retryAfter)
                            notify(discordNotification, webhookToken)
                            return@forEach
                        }
                    }
                }
                throw OpenSeaMonitorException("Error while sending discord notification: ${e.message}")
                    .also { logger.error { it.message } }
            }
            Thread.sleep(SEND_DELAY_MS)
        }
    }

    private fun formatWalletAddress(address: String): String {
        val walletStart = address.substring(0, 4)
        val walletEnd = address.subSequence(address.length - 4, address.length)

        return "$walletStart...$walletEnd"
    }

    private fun getNoticationType(eventType: EventType): String {
        return when (eventType) {
            EventType.CREATED -> "Listed"
            EventType.SUCCESSFUL -> "Sold"
        }
    }
    private fun formatPrice(price: Double?): Double? {
        return price?.toDouble()?.div(1_000_000_000_000_000_000)
    }

    fun notify(discordNotification: DiscordNotification, webhookToken: String): ResponseEntity<Void> {
        return discordNotificationClient.notify(discordNotification, webhookToken)
    }

    fun handleRateLimit(response: ResponseEntity<Void>) {

        val headers: HttpHeaders = response.headers

        val rateLimitRemaining: String? = headers[X_RATE_LIMIT_REMAINING_HEADER]?.firstOrNull()

        if (rateLimitRemaining != null && rateLimitRemaining.toInt() == 0) {

            val rateLimitResetEpochSeconds: String? = headers[X_RATE_LIMIT_RESET_HEADER]?.firstOrNull()

            if (rateLimitResetEpochSeconds.isNullOrBlank()) {
                Thread.sleep(2)
                return
            }

            val currentMillis = System.currentTimeMillis()
            var msToWait = (rateLimitResetEpochSeconds.toLong().times(1000)).minus(currentMillis)

            logger.info { "Rate Limit info: rateLimitRemaining: ${rateLimitRemaining} | currentMillis: ${currentMillis} | rateLimitResetEpochSeconds: ${rateLimitResetEpochSeconds} | msToWait: ${msToWait}" }

            if (msToWait > 0) {
                msToWait = if (msToWait < 2000) 2000 else msToWait
                logger.info { "Rate limit hit, waiting for ${msToWait}ms before making next call" }
                Thread.sleep(msToWait)
                return
            }
        }
    }
}