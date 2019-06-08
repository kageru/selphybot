package moe.kageru.kagebot

import org.javacord.api.entity.message.MessageAuthor
import moe.kageru.kagebot.Util.doIf

private const val AUTHOR_PLACEHOLDER = "@@"

class Command(trigger: String?, response: String?, matchType: MatchType?) {
    val trigger: String = trigger!!
    val regex: Regex? = if (matchType == MatchType.REGEX) Regex(trigger!!) else null
    private val response: String = response!!
    private val matchType: MatchType = matchType ?: MatchType.PREFIX

    constructor(cmd: Command) : this(cmd.trigger, cmd.response, cmd.matchType)

    fun matches(msg: String) = this.matchType.matches(msg, this)
    fun respond(author: MessageAuthor) = this.response.doIf({ it.contains(AUTHOR_PLACEHOLDER) }) {
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
