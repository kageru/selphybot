package moe.kageru.kagebot.command

import arrow.core.Option
import arrow.core.toOption
import moe.kageru.kagebot.Util
import org.javacord.api.event.message.MessageCreateEvent

class Permissions(
  hasOneOf: List<String>?,
  hasNoneOf: List<String>?,
  private val onlyDM: Boolean = false,
) {
  private val hasOneOf: Option<Set<String>> = hasOneOf?.toSet().toOption()
  private val hasNoneOf: Option<Set<String>> = hasNoneOf?.toSet().toOption()

  fun isAllowed(message: MessageCreateEvent): Boolean = when {
    message.messageAuthor.isBotOwner -> true
    onlyDM && !message.isPrivateMessage -> false
    // returns true if the Option is empty (case for no restrictions)
    else -> hasOneOf.forall { Util.hasOneOf(message.messageAuthor, it) } &&
      hasNoneOf.forall { !Util.hasOneOf(message.messageAuthor, it) }
  }
}
