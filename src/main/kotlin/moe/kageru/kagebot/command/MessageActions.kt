package moe.kageru.kagebot.command

import moe.kageru.kagebot.Globals.config
import moe.kageru.kagebot.Log.log
import moe.kageru.kagebot.MessageUtil
import moe.kageru.kagebot.config.RawMessageActions
import org.javacord.api.event.message.MessageCreateEvent

class MessageActions(rawActions: RawMessageActions) {
    private val delete: Boolean = rawActions.delete
    private val redirect: MessageRedirect? = rawActions.redirect?.let(::MessageRedirect)
    private val assignment: RoleAssignment? = rawActions.assign?.let(::RoleAssignment)

    fun run(message: MessageCreateEvent, command: Command) {
        if (delete) {
            deleteMessage(message)
        }
        redirect?.execute(message, command)
        assignment?.assign(message)
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

