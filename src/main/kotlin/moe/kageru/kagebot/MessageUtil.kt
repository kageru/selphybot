package moe.kageru.kagebot

import org.javacord.api.entity.message.MessageAuthor
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.user.User

object MessageUtil {
    fun mention(user: MessageAuthor): String {
        return "<@${user.id}>"
    }

    fun mention(user: User): String {
        return "<@${user.id}>"
    }

    fun getEmbedBuilder(): EmbedBuilder {
        val builder = EmbedBuilder()
        Globals.server.icon.ifPresent { builder.setThumbnail(it) }
        return builder.setColor(Globals.systemConfig.color).setTimestampToNow()
    }

    fun mapToEmbed(contents: Map<String, String>): EmbedBuilder {
        val builder = getEmbedBuilder()
        for ((heading, content) in contents) {
            builder.addField(heading.removePrefix("\"").removeSuffix("\""), content)
        }
        return builder
    }
}