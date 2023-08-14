package moe.kageru.kagebot.command

import com.fasterxml.jackson.annotation.JsonProperty
import moe.kageru.kagebot.Log
import moe.kageru.kagebot.Util
import moe.kageru.kagebot.extensions.getUser
import moe.kageru.kagebot.extensions.unwrap
import org.javacord.api.event.message.MessageCreateEvent

class RoleAssignment(@JsonProperty("role") role: String) {
  private val role = Util.findRole(role).unwrap()

  fun assign(message: MessageCreateEvent) = message.getUser().fold(
    { Log.warn("Could not find user ${message.messageAuthor.name} for role assign") },
    { it.addRole(role, "Requested via command.") },
  )
}
