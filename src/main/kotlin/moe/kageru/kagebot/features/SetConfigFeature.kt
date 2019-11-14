package moe.kageru.kagebot.features

import moe.kageru.kagebot.MessageUtil.sendEmbed
import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.config.ConfigParser
import org.javacord.api.event.message.MessageCreateEvent

class SetConfigFeature : MessageFeature {
  @ExperimentalStdlibApi
  override fun handle(message: MessageCreateEvent) {
    if (message.messageAttachments.size != 1) {
      message.channel.sendMessage("Error: please attach the new config to your message.")
      return
    }
    val newConfig = message.messageAttachments[0].url.openStream().readAllBytes().decodeToString()
    try {
      Config.localization = Config.localeSpec.string(newConfig)
      Config.featureConfig = Config.featureSpec.string(newConfig)
      Config.commandConfig = Config.commandSpec.string(newConfig)
      ConfigParser.configFile.writeText(newConfig)
      message.channel.sendMessage("Config reloaded.")
    } catch (e: Exception) {
      message.channel.sendEmbed {
        addField("Error", "```${e.message}```")
      }
    }
  }
}
