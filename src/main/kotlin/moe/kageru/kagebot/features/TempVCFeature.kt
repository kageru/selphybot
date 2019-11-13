package moe.kageru.kagebot.features

import arrow.core.Either
import arrow.core.filterOrElse
import arrow.core.flatMap
import arrow.core.rightIfNotNull
import com.fasterxml.jackson.annotation.JsonProperty
import moe.kageru.kagebot.Log
import moe.kageru.kagebot.Util.asOption
import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.extensions.categoriesByName
import moe.kageru.kagebot.persistence.Dao
import org.javacord.api.DiscordApi
import org.javacord.api.entity.channel.ChannelCategory
import org.javacord.api.entity.channel.ServerVoiceChannel
import org.javacord.api.event.message.MessageCreateEvent

class TempVCFeature(@JsonProperty("category") category: String? = null) : EventFeature, MessageFeature {
    private val category: ChannelCategory? = category?.let { Config.server.categoriesByName(it).first() }

    override fun handle(message: MessageCreateEvent): Unit = with(message) {
        Either.cond(' ' in readableMessageContent,
            { readableMessageContent.split(' ', limit = 2).last() },
            { "Invalid syntax, expected `<command> <userlimit>`" })
            .flatMap { limit ->
                limit.toIntOrNull().rightIfNotNull { "Invalid syntax, expected a number as limit, got $limit" }
            }.filterOrElse({ it < 99 }, { "Error: can’t create a channel with that many users." })
            .fold({ err -> channel.sendMessage(err) },
                { limit ->
                    createChannel(message, limit)
                    channel.sendMessage("Done")
                })
    }

    override fun register(api: DiscordApi) {
        api.addServerVoiceChannelMemberLeaveListener { event ->
            if (event.channel.connectedUsers.isEmpty() && Dao.isTemporaryVC(event.channel.idAsString)) {
                deleteChannel(event.channel)
            }
        }
    }

    private fun deleteChannel(channel: ServerVoiceChannel) =
        channel.delete("Empty temporary channel").asOption().fold(
            { Log.warn("Attempted to delete temporary VC without the necessary permissions") },
            { Dao.removeTemporaryVC(channel.idAsString) }
        )

    private fun createChannel(message: MessageCreateEvent, limit: Int): Unit =
        Config.server.createVoiceChannelBuilder().apply {
            setUserlimit(limit)
            setName(generateChannelName(message))
            setAuditLogReason("Created temporary VC for user ${message.messageAuthor.discriminatedName}")
            setCategory(category)
        }.create().asOption().fold(
            { Log.warn("Attempted to create temporary VC without the necessary permissions") },
            { channel -> Dao.addTemporaryVC(channel.idAsString) })

    private fun generateChannelName(message: MessageCreateEvent): String =
        "${message.messageAuthor.name}’s volatile corner"
}
