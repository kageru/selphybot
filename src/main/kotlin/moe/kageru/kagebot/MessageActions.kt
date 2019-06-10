package moe.kageru.kagebot

import moe.kageru.kagebot.Config.Companion.config
import moe.kageru.kagebot.Log.log
import moe.kageru.kagebot.Util.ifNotEmpty
import org.javacord.api.event.message.MessageCreateEvent

class MessageActions(private val delete: Boolean, private val redirect: Redirect?) {
    fun run(message: MessageCreateEvent, command: Command) {
        if (delete && message.message.canYouDelete()) {
            message.deleteMessage()
            message.messageAuthor.asUser().ifNotEmpty { user ->
                user.sendMessage(
                    MessageUtil.getEmbedBuilder()
                        .addField("Blacklisted", config.localization.messageDeleted)
                        .addField("Original:", "“${message.readableMessageContent}”")
                )
            }
        }
        redirect?.execute(message, command)
    }
}

class Redirect(private val target: Long, private val anonymous: Boolean) {

    fun execute(message: MessageCreateEvent, command: Command) {
        val embed = MessageUtil.getEmbedBuilder()
            .addField(
                config.localization.redirectedMessage,
                message.readableMessageContent.let { content ->
                    when (command.matchType) {
                        MatchType.PREFIX -> content.removePrefix(command.trigger).trim()
                        else -> content
                    }
                }
            )
        // No inlined if/else because the types are different.
        // Passing the full message author will also include the avatar in the embed.
        embed.apply {
            if (anonymous) {
                setAuthor("Anonymous")
            } else {
                setAuthor(message.messageAuthor)
            }
        }
        Config.server!!.getTextChannelById(target).ifNotEmpty { it.sendMessage(embed) }
            ?: log.warning("Could not redirect message to channel $target")
    }
}