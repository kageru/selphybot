package moe.kageru.kagebot.command

import moe.kageru.kagebot.Util
import org.javacord.api.entity.permission.Role
import org.javacord.api.event.message.MessageCreateEvent

class Permissions(
    hasOneOf: List<String>?,
    hasNoneOf: List<String>?,
    private val onlyDM: Boolean = false
) {
    private val hasOneOf: Set<Role>? = hasOneOf?.mapTo(mutableSetOf(), Util::findRole)
    private val hasNoneOf: Set<Role>? = hasNoneOf?.mapTo(mutableSetOf(), Util::findRole)

    fun isAllowed(message: MessageCreateEvent): Boolean {
        if (message.messageAuthor.isBotOwner) {
            return true
        }
        if (onlyDM && !message.isPrivateMessage) {
            return false
        }
        hasOneOf?.let { roles ->
            if (!Util.hasOneOf(message.messageAuthor, roles)) return false
        }
        hasNoneOf?.let { roles ->
            if (Util.hasOneOf(message.messageAuthor, roles)) return false
        }
        return true
    }
}
