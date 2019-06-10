package moe.kageru.kagebot

import moe.kageru.kagebot.Config.Companion.config
import org.javacord.api.entity.message.MessageAuthor
import org.javacord.api.entity.message.embed.EmbedBuilder
import java.awt.Color

object MessageUtil {
    fun mention(user: MessageAuthor): String {
        return "<@${user.id}>"
    }

    fun getEmbedBuilder(): EmbedBuilder {
        val builder = EmbedBuilder()
        Config.server!!.icon.ifPresent { builder.setThumbnail(it) }
        return builder.setColor(Color.decode(config.system.color)).setTimestampToNow()
    }
}