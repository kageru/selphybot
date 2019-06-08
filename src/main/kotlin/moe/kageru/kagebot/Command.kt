package moe.kageru.kagebot

import moe.kageru.kagebot.Util.doIf
import moe.kageru.kagebot.Util.asString
import org.javacord.api.entity.message.MessageAuthor
import org.javacord.api.event.message.MessageCreateEvent

private const val AUTHOR_PLACEHOLDER = "@@"

class Command(trigger: String?, response: String?, matchType: MatchType?, private val deleteMessage: Boolean) {
    val trigger: String = trigger!!
    val regex: Regex? = if (matchType == MatchType.REGEX) Regex(trigger!!) else null
    private val response: String = response!!
    private val matchType: MatchType = matchType ?: MatchType.PREFIX

    constructor(cmd: Command) : this(cmd.trigger, cmd.response, cmd.matchType, cmd.deleteMessage)

    fun execute(message: MessageCreateEvent) {
        if (this.deleteMessage && message.isServerMessage) {
            val wasDeleted = message.deleteMessage()
            if (wasDeleted.isCompletedExceptionally) {
                Log.log.warning("Could not delete message ${message.asString()}")
            }
        }
        message.channel.sendMessage(respond(message.messageAuthor))
    }

    fun matches(msg: String) = this.matchType.matches(msg, this)
    private fun respond(author: MessageAuthor) = this.response.doIf({ it.contains(AUTHOR_PLACEHOLDER) }) {
        it.replace(AUTHOR_PLACEHOLDER, MessageUtil.mention(author))
    }
}

enum class MatchType {
    PREFIX {
        override fun matches(message: String, command: Command) = message.startsWith(command.trigger)
    },
    FULL {
        override fun matches(message: String, command: Command) = message == command.trigger
    },
    CONTAINS {
        override fun matches(message: String, command: Command) = message.contains(command.trigger)
    },
    REGEX {
        override fun matches(message: String, command: Command) = command.regex!!.matches(message)
    };

    abstract fun matches(message: String, command: Command): Boolean
}
