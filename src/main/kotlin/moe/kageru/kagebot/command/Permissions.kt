package moe.kageru.kagebot.command

import arrow.core.Option
import moe.kageru.kagebot.Util
import moe.kageru.kagebot.extensions.unwrap
import org.javacord.api.entity.permission.Role
import org.javacord.api.event.message.MessageCreateEvent

class Permissions(
  hasOneOf: List<String>?,
  hasNoneOf: List<String>?,
  private val onlyDM: Boolean = false
) {
  private val hasOneOf: Option<Set<Role>> = resolveRoles(hasOneOf)
  private val hasNoneOf: Option<Set<Role>> = resolveRoles(hasNoneOf)

  private fun resolveRoles(hasOneOf: List<String>?): Option<Set<Role>> {
    return Option.fromNullable(hasOneOf?.mapTo(mutableSetOf(), { Util.findRole(it).unwrap() }))
  }

  fun isAllowed(message: MessageCreateEvent): Boolean = when {
    message.messageAuthor.isBotOwner -> true
    onlyDM && !message.isPrivateMessage -> false
    // returns true if the Option is empty (case for no restrictions)
    else -> hasOneOf.forall { Util.hasOneOf(message.messageAuthor, it) } &&
      hasNoneOf.forall { !Util.hasOneOf(message.messageAuthor, it) }
  }
}
