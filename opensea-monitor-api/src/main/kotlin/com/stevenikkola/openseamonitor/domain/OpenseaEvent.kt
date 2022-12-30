package com.stevenikkola.openseamonitor.domain

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document("opensea-events")

data class OpenseaEvent(
    val id: String,
    val asset: OpenseaAsset,
    val auctionType: String?,
    val contractAddress: String,
    val createdDate: LocalDateTime,
    val endingPrice: String?,
    val eventType: String,
    val isPrivate: Boolean,
    val payment_token: PaymentToken,
    val seller: AssetSeller,
    val startingPrice: String?,
    val total_price: String?,
    @JsonProperty("winner_account")
    val buyer: AssetBuyer?,
)

data class OpenseaAsset(
    val id: Int,
    val tokenId: String,
    val imageUrl: String?,
    val imagePreviewUrl: String?,
    val imageThumbnailUrl: String?,
    val imageOriginalUrl: String?,
    val name: String?,
    val permalink: String,
    val collection: OpenseaCollection,
    val tokenMetadata: String?,
    val sellOrders: List<SellOrder>? = listOf(),
)

data class OpenseaCollection(
    val imageUrl: String?,
    val slug: String?
)

data class AssetSeller(
    val user: AssetUser?,
    val address: String
)

data class AssetUser(
    val username: String?,
)

data class PaymentToken(
    val symbol: String,
    val name: String,
    val ethPrice: String,
    val usdPrice: String,
)

data class AssetBuyer(
    val user: AssetUser?,
    val address: String
)

data class SellOrder(
    val expirationTime: Long?,
    val currentPrice: String?,
    val paymentTokenContract: PaymentTokenContract?
)

data class PaymentTokenContract(
    val symbol: String?
)