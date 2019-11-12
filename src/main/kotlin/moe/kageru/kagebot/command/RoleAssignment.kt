package moe.kageru.kagebot.command

import com.fasterxml.jackson.annotation.JsonProperty
import moe.kageru.kagebot.Log
import moe.kageru.kagebot.Util
import moe.kageru.kagebot.Util.getUser
import moe.kageru.kagebot.Util.unwrap
import org.javacord.api.event.message.MessageCreateEvent

class RoleAssignment(@JsonProperty("role") role: String) {
    private val role = Util.findRole(role).unwrap()

    fun assign(message: MessageCreateEvent) =
        message.getUser()?.addRole(role, "Requested via command.")
            ?: Log.warn("Could not find user ${message.messageAuthor.name} for role assign")
}
