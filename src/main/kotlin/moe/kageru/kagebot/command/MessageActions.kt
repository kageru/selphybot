package moe.kageru.kagebot.command

import moe.kageru.kagebot.Globals.config
import moe.kageru.kagebot.Log.log
import moe.kageru.kagebot.MessageUtil
import moe.kageru.kagebot.Util
import moe.kageru.kagebot.config.RawMessageActions
import moe.kageru.kagebot.config.RawRedirect
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.event.message.MessageCreateEvent

class MessageActions(rawActions: RawMessageActions) {
    private val delete: Boolean = rawActions.delete
    private val redirect: Redirect? = rawActions.redirect?.let { Redirect(it) }

    fun run(message: MessageCreateEvent, command: Command) {
        if (delete) {
            deleteMessage(message)
        }
        redirect?.execute(message, command)
    }

    private fun deleteMessage(message: MessageCreateEvent) {
        if (message.message.canYouDelete()) {
            message.deleteMessage()
            message.messageAuthor.asUser().ifPresent { user ->
                user.sendMessage(
                    MessageUtil.getEmbedBuilder()
                        .addField("Blacklisted", config.localization.messageDeleted)
                        .addField("Original:", "“${message.readableMessageContent}”")
                )
            }
        } else {
            log.info("Tried to delete a message without the necessary permissions. Channel: ${message.channel.id}")
        }
    }
}

class Redirect(rawRedirect: RawRedirect) {
    private val target: TextChannel = rawRedirect.target?.let(Util::findChannel)
        ?: throw IllegalArgumentException("Every redirect needs to have a target.")
    private val anonymous: Boolean = rawRedirect.anonymous

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

        if (target.sendMessage(embed).isCompletedExceptionally) {
            log.warning("Could not redirect message to channel $target")
        }
    }
}