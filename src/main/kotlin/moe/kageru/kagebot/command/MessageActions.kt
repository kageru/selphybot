package moe.kageru.kagebot.command

import com.fasterxml.jackson.annotation.JsonProperty
import moe.kageru.kagebot.Log
import moe.kageru.kagebot.MessageUtil.sendEmbed
import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.config.LocalizationSpec
import org.javacord.api.event.message.MessageCreateEvent

class MessageActions(
  private val delete: Boolean = false,
  private val redirect: MessageRedirect?,
  @JsonProperty("assign")
  private val assignment: RoleAssignment?,
) {

  fun run(message: MessageCreateEvent, command: Command) {
    if (delete) {
      deleteMessage(message)
    }
    redirect?.execute(message, command)
    assignment?.assign(message)
  }

  private fun deleteMessage(message: MessageCreateEvent) {
    if (message.message.canYouDelete()) {
      message.deleteMessage()
      message.messageAuthor.asUser().ifPresent { user ->
        user.sendEmbed {
          addField("__Blacklisted__", Config.localization[LocalizationSpec.messageDeleted])
          addField("Original:", "“${message.readableMessageContent}”")
        }
      }
    } else {
      Log.info("Tried to delete a message without the necessary permissions. Channel: ${message.channel.id}")
    }
  }
}
