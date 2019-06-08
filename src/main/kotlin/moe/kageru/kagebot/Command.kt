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
    private val deleteMessage: Boolean,
    neededPermissions: Iterable<Long>?
) {
    val trigger: String = trigger!!
    val regex: Regex? = if (matchType == MatchType.REGEX) Regex(trigger!!) else null
    private val matchType: MatchType = matchType ?: MatchType.PREFIX
    private val neededRoles = neededPermissions?.toSet()

    constructor(cmd: Command) : this(
        cmd.trigger,
        cmd.response,
        cmd.matchType,
        cmd.deleteMessage,
        cmd.neededRoles
    )

    fun execute(message: MessageCreateEvent) {
        neededRoles?.let { roles ->
            if (!(message.messageAuthor.isBotOwner || hasOneOf(message.messageAuthor, roles))) {
                message.channel.sendMessage(config.localization.permissionDenied)
                return
            }
        }
        if (this.deleteMessage && message.message.canYouDelete()) {
            message.deleteMessage()
        }
        this.response?.let {
            message.channel.sendMessage(respond(message.messageAuthor))
        }
    }

    private fun hasOneOf(messageAuthor: MessageAuthor, roles: Set<Long>): Boolean {
        val optional = messageAuthor.asUser()
        return when {
            optional.isEmpty -> false
            else -> optional.get().getRoles(Config.server).map { it.id }.toSet().intersect(roles).isNotEmpty()
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
