package moe.kageru.kagebot

import moe.kageru.kagebot.Config.Companion.config
import moe.kageru.kagebot.Util.doIf
import org.javacord.api.entity.message.MessageAuthor
import org.javacord.api.event.message.MessageCreateEvent

private const val AUTHOR_PLACEHOLDER = "@@"

class Command(
    trigger: String?,
    private val response: String?,
    matchType: MatchType?,
    private val permissions: Permissions?,
    private val actions: MessageActions?
) {
    val trigger: String = trigger!!
    val regex: Regex? = if (matchType == MatchType.REGEX) Regex(trigger!!) else null
    val matchType: MatchType = matchType ?: MatchType.PREFIX

    constructor(cmd: Command) : this(
        cmd.trigger,
        cmd.response,
        cmd.matchType,
        cmd.permissions?.let { Permissions(it) },
        cmd.actions
    )

    fun execute(message: MessageCreateEvent) {
        if (!(message.messageAuthor.isBotOwner || permissions?.isAllowed(message) != false)) {
            message.channel.sendMessage(config.localization.permissionDenied)
            return
        }
        this.actions?.run(message, this)
        this.response?.let {
            message.channel.sendMessage(respond(message.messageAuthor))
        }
    }

    fun matches(msg: String) = this.matchType.matches(msg, this)
    private fun respond(author: MessageAuthor) = this.response!!.doIf({ it.contains(AUTHOR_PLACEHOLDER) }) {
        it.replace(AUTHOR_PLACEHOLDER, MessageUtil.mention(author))
    }
}

enum class MatchType {
    PREFIX {
        override fun matches(message: String, command: Command) = message.startsWith(command.trigger)
    },
    CONTAINS {
        override fun matches(message: String, command: Command) = message.contains(command.trigger)
    },
    REGEX {
        override fun matches(message: String, command: Command) = command.regex!!.matches(message)
    };

    abstract fun matches(message: String, command: Command): Boolean
}

class Permissions(hasOneOf: Iterable<Long>?, hasNoneOf: Iterable<Long>?, private val onlyDM: Boolean) {
    private val hasNoneOf = hasNoneOf?.toSet()
    private val hasOneOf = hasOneOf?.toSet()

    constructor(perms: Permissions) : this(perms.hasOneOf, perms.hasNoneOf, perms.onlyDM)

    fun isAllowed(message: MessageCreateEvent): Boolean {
        if (onlyDM && !message.isPrivateMessage) {
            return false
        }
        hasOneOf?.let {
            if (!Util.hasOneOf(message.messageAuthor, hasOneOf)) return false
        }
        hasNoneOf?.let {
            if (Util.hasOneOf(message.messageAuthor, hasNoneOf)) return false
        }
        return true
    }
}