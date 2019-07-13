package moe.kageru.kagebot

import moe.kageru.kagebot.config.Config
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
        Config.server.icon.ifPresent { builder.setThumbnail(it) }
        return builder.setColor(Config.systemConfig.color).setTimestampToNow()
    }

    /*
     * The reason we use a list here (rather than a map) is that maps would not retain the order specified in the config.
     * I tried LinkedHashMaps, but those don’t seem to work either.
     */
    fun listToEmbed(contents: List<String>): EmbedBuilder {
        if (contents.size % 2 == 1) {
            throw IllegalStateException("Embed must have even number of content strings (title/content pairs)")
        }
        val builder = getEmbedBuilder()
        contents.zip(1..contents.size).filter { it.second % 2 == 0 }
        for ((heading, content) in contents.withIndex().filter { it.index % 2 == 0 }
                zip contents.withIndex().filter { it.index % 2 == 1 }) {
            builder.addField(heading.value, content.value)
        }
        return builder
    }
}
