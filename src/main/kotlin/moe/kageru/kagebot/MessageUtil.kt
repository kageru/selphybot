package moe.kageru.kagebot

import org.javacord.api.entity.message.MessageAuthor
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.event.message.MessageCreateEvent
import java.awt.Color
import moe.kageru.kagebot.Config.Companion.config

object MessageUtil {
    fun mention(user: MessageAuthor): String {
        return "<@${user.id}>"
    }

    fun MessageCreateEvent.asString(): String =
        "<${this.messageAuthor.discriminatedName}> ${this.readableMessageContent}"

    fun getEmbedBuilder(): EmbedBuilder {
        val builder = EmbedBuilder()
        Config.server!!.icon.ifPresent { builder.setThumbnail(it) }
        return builder.setColor(Color.decode(config.system.color)).setTimestampToNow()
    }
}