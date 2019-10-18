package moe.kageru.kagebot.features

import moe.kageru.kagebot.Log
import moe.kageru.kagebot.MessageUtil.sendEmbed
import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.config.ConfigParser
import moe.kageru.kagebot.config.RawConfig
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.event.message.MessageCreateEvent

class SetConfigFeature : MessageFeature {
    @ExperimentalStdlibApi
    override fun handle(message: MessageCreateEvent) {
        if (message.messageAttachments.size != 1) {
            message.channel.sendMessage("Error: please attach the new config to your message.")
            return
        }
        val newConfig = message.messageAttachments[0].url.openStream().readAllBytes().decodeToString()
        val rawConfig = try {
            RawConfig.readFromString(newConfig)
        } catch (e: IllegalStateException) {
            reportError(message.channel, e)
            return
        }
        try {
            Config.localization = Config.localeSpec.string(newConfig)
            ConfigParser.reloadFeatures(rawConfig)
            Config.commandConfig = Config.commandSpec.string(newConfig)
            ConfigParser.configFile.writeText(newConfig)
            message.channel.sendMessage("Config reloaded.")
        } catch (e: IllegalArgumentException) {
            message.channel.sendEmbed {
                addField("Error", "```${e.message}```")
            }
        }
    }

    private fun reportError(message: TextChannel, e: IllegalStateException) {
        message.sendEmbed {
            addField(
                "An unexpected error occured. This is probably caused by a malformed config file. Perhaps this can help:",
                "```$e: ${e.message}"
            )
        }
        Log.info("Could not parse new config: $e: ${e.message}")
        return
    }
}
