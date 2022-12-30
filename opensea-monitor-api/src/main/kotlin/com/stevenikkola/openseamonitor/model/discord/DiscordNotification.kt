package com.stevenikkola.openseamonitor.model.discord

data class DiscordNotification(
    val content: String? = null,
    val embeds: List<DiscordEmbed>
)

data class DiscordEmbed(
    val author: DiscordAuthor,
    val title: String?,
    val thumbnail: DiscordEmbedThumbnail,
    val url: String,
    val color: Int? = 3115751,
    val fields: List<DiscordEmbedField>,
    val footer: DiscordEmbedFooter,
)

data class DiscordAuthor(
    val name: String?,
    val iconUrl: String?
)
data class DiscordEmbedThumbnail(
    val url: String?
)

data class DiscordEmbedField(
    val name: String,
    val value: String?,
    val inline: Boolean
)

data class DiscordEmbedFooter(
    val text: String,
    val iconUrl: String
)
