package moe.kageru.kagebot.command

import moe.kageru.kagebot.Util
import moe.kageru.kagebot.config.RawPermissions
import org.javacord.api.entity.permission.Role
import org.javacord.api.event.message.MessageCreateEvent

class Permissions(perms: RawPermissions) {
    private val hasOneOf: Set<Role>?
    private val hasNoneOf: Set<Role>?
    private val onlyDM: Boolean

    init {
        hasOneOf = perms.hasOneOf?.mapTo(mutableSetOf(), Util::findRole)
        hasNoneOf = perms.hasNoneOf?.mapTo(mutableSetOf(), Util::findRole)
        onlyDM = perms.onlyDM
    }

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
