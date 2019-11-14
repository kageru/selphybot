package moe.kageru.kagebot.features

import moe.kageru.kagebot.config.ConfigParser
import org.javacord.api.event.message.MessageCreateEvent

/**
 * Simple message handler to send the current config file via message attachment.
 */
class GetConfigFeature : MessageFeature {
  override fun handle(message: MessageCreateEvent) {
    message.channel.sendMessage(ConfigParser.configFile)
  }
}
