package moe.kageru.kagebot.features

import com.fasterxml.jackson.annotation.JsonProperty
import moe.kageru.kagebot.Log
import moe.kageru.kagebot.Util.failed
import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.persistence.Dao
import org.javacord.api.DiscordApi
import org.javacord.api.entity.channel.ChannelCategory
import org.javacord.api.entity.channel.ServerVoiceChannel
import org.javacord.api.event.message.MessageCreateEvent
import java.util.concurrent.CompletionException

class TempVCFeature(@JsonProperty("category") category: String? = null) : EventFeature, MessageFeature {
    private val category: ChannelCategory? =
        category?.let { Config.server.getChannelCategoriesByNameIgnoreCase(it).first() }

    override fun handle(message: MessageCreateEvent) {
        if (" " !in message.readableMessageContent) {
            message.channel.sendMessage("Invalid syntax, expected 2 arguments")
            return
        }
        val (_, limit) = message.readableMessageContent.split(" ", limit = 2)
        limit.toLongOrNull()?.let { parsedLimit ->
            if (parsedLimit > 99) {
                message.channel.sendMessage("You can’t create a channel with that many users.")
            }
            createChannel(message, parsedLimit)
            message.channel.sendMessage("Done")
        } ?: message.channel.sendMessage("Invalid syntax, expected a number, got $limit")
    }

    override fun register(api: DiscordApi) {
        api.addServerVoiceChannelMemberLeaveListener { event ->
            if (event.channel.connectedUsers.isEmpty() && Dao.isTemporaryVC(event.channel.idAsString)) {
                deleteChannel(event.channel)
            }
        }
    }

    private fun deleteChannel(channel: ServerVoiceChannel) {
        val deletion = channel.delete("Empty temporary channel")
        if (deletion.failed()) {
            Log.warn("Attempted to delete temporary VC without the necessary permissions")
        } else {
            Dao.removeTemporaryVC(channel.idAsString)
        }
    }

    private fun createChannel(message: MessageCreateEvent, limit: Long) {
        val creation = Config.server.createVoiceChannelBuilder().apply {
            setUserlimit(limit.toInt())
            setName(generateChannelName(message))
            setAuditLogReason("Created temporary VC for user ${message.messageAuthor.discriminatedName}")
            category?.let { setCategory(it) }
        }.create()
        try {
            val channel = creation.join()
            Dao.addTemporaryVC(channel.idAsString)
        } catch (e: CompletionException) {
            Log.warn("Attempted to create temporary VC without the necessary permissions")
        }
    }

    private fun generateChannelName(message: MessageCreateEvent): String {
        return "${message.messageAuthor.name}’s volatile corner"
    }
}