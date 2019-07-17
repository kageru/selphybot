package moe.kageru.kagebot.command

import moe.kageru.kagebot.Log
import moe.kageru.kagebot.Util
import moe.kageru.kagebot.config.RawAssignment
import org.javacord.api.event.message.MessageCreateEvent

internal class RoleAssignment(rawAssignment: RawAssignment) {
    private val role = rawAssignment.role?.let { idOrName ->
        Util.findRole(idOrName)
    } ?: throw IllegalArgumentException("Can’t find role “${rawAssignment.role}”")

    fun assign(message: MessageCreateEvent) {
        Util.userFromMessage(message)?.addRole(role, "Requested via command.")
            ?: Log.warn("Could not find user ${message.messageAuthor.name} for role assign")
    }
}
