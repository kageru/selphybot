package moe.kageru.kagebot

import moe.kageru.kagebot.Config.Companion.config
import moe.kageru.kagebot.Util.doIf
import moe.kageru.kagebot.Util.ifNotEmpty
import org.javacord.api.entity.message.MessageAuthor
import org.javacord.api.event.message.MessageCreateEvent

private const val AUTHOR_PLACEHOLDER = "@@"

class Command(
    trigger: String?,
    private val response: String?,
    matchType: MatchType?,
    neededPermissions: Iterable<Long>?,
    private val actions: MessageActions?
) {
    val trigger: String = trigger!!
    val regex: Regex? = if (matchType == MatchType.REGEX) Regex(trigger!!) else null
    val matchType: MatchType = matchType ?: MatchType.PREFIX
    private val neededRoles = neededPermissions?.toSet()

    constructor(cmd: Command) : this(
        cmd.trigger,
        cmd.response,
        cmd.matchType,
        cmd.neededRoles,
        cmd.actions
    )

    fun execute(message: MessageCreateEvent) {
        neededRoles?.let { roles ->
            if (!(message.messageAuthor.isBotOwner || hasOneOf(message.messageAuthor, roles))) {
                message.channel.sendMessage(config.localization.permissionDenied)
                return
            }
        }
        this.actions?.run(message, this)
        this.response?.let {
            message.channel.sendMessage(respond(message.messageAuthor))
        }
    }

    private fun hasOneOf(messageAuthor: MessageAuthor, roles: Set<Long>): Boolean {
        return messageAuthor.asUser().ifNotEmpty { user ->
            user.getRoles(Config.server).map { it.id }.toSet().intersect(roles).isNotEmpty()
        } ?: false
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
