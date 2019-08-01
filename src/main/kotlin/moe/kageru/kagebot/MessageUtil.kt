package moe.kageru.kagebot

import moe.kageru.kagebot.config.Config
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.MessageAuthor
import org.javacord.api.entity.message.Messageable
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.user.User
import java.util.concurrent.CompletableFuture

object MessageUtil {
    fun mention(user: MessageAuthor): String {
        return "<@${user.id}>"
    }

    fun mention(user: User): String {
        return "<@${user.id}>"
    }

    fun withEmbed(op: EmbedBuilder.() -> Unit): EmbedBuilder {
        val builder = EmbedBuilder()
        Config.server.icon.ifPresent { builder.setThumbnail(it) }
        builder.setColor(Config.systemConfig.color)
        builder.op()
        return builder
    }

    fun Messageable.sendEmbed(op: EmbedBuilder.() -> Unit) {
        val embed = withEmbed {
            setTimestampToNow()
            op()
        }
        sendMessage(embed)
    }

    /**
     * Send and embed and add the current time to it.
     * The time is not set in [withEmbed] because of https://git.kageru.moe/kageru/discord-kagebot/issues/13.
     */
    fun sendEmbed(target: Messageable, embed: EmbedBuilder): CompletableFuture<Message> {
        return target.sendMessage(embed.setTimestampToNow())
    }

    /**
     * The reason we use a list here (rather than a map) is that maps would not retain the order specified in the config.
     * I tried LinkedHashMaps, but those don’t seem to work either.
     */
    fun listToEmbed(contents: List<String>): EmbedBuilder {
        if (contents.size % 2 == 1) {
            throw IllegalStateException("Embed must have even number of content strings (title/content pairs)")
        }
        return withEmbed {
            contents.zipWithNext().forEach { (heading, content) ->
                addField(heading, content)
            }
        }
    }
}
