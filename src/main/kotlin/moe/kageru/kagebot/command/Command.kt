package moe.kageru.kagebot.command

import moe.kageru.kagebot.Globals.config
import moe.kageru.kagebot.MessageUtil
import moe.kageru.kagebot.Util.doIf
import moe.kageru.kagebot.config.RawCommand
import org.javacord.api.entity.message.MessageAuthor
import org.javacord.api.event.message.MessageCreateEvent

private const val AUTHOR_PLACEHOLDER = "@@"

class Command(cmd: RawCommand) {
    val trigger: String
    private val response: String?
    val matchType: MatchType
    private val permissions: Permissions?
    private val actions: MessageActions?
    val regex: Regex?

    init {
        trigger = cmd.trigger ?: throw IllegalArgumentException("Every command must have a trigger.")
        response = cmd.response
        matchType = cmd.matchType?.let { type ->
            MatchType.values().find { it.name.equals(type, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invalid [command.matchType]: “${cmd.matchType}”")
        } ?: MatchType.PREFIX
        permissions = cmd.permissions?.let { Permissions(it) }
        actions = cmd.actions?.let { MessageActions(it) }
        regex = if (matchType == MatchType.REGEX) Regex(trigger) else null
    }

    fun execute(message: MessageCreateEvent) {
        if (permissions?.isAllowed(message) == false) {
            if (config.localization.permissionDenied.isNotBlank()) {
                message.channel.sendMessage(config.localization.permissionDenied)
            }
            return
        }
        this.actions?.run(message, this)
        this.response?.let {
            message.channel.sendMessage(respond(message.messageAuthor, it))
        }
    }

    fun matches(msg: String) = this.matchType.matches(msg, this)
    private fun respond(author: MessageAuthor, response: String) = response.doIf({ it.contains(AUTHOR_PLACEHOLDER) }) {
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
