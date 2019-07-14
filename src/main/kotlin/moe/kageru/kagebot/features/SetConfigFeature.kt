package moe.kageru.kagebot.features

import moe.kageru.kagebot.Log
import moe.kageru.kagebot.MessageUtil
import moe.kageru.kagebot.config.ConfigParser
import moe.kageru.kagebot.config.RawConfig
import org.javacord.api.event.message.MessageCreateEvent
import java.lang.IllegalArgumentException

class SetConfigFeature : MessageFeature() {
    @ExperimentalStdlibApi
    override fun handleInternal(message: MessageCreateEvent) {
        if (message.messageAttachments.size != 1) {
            message.channel.sendMessage("Error: please attach the new config to your message.")
            return
        }
        val newConfig = message.messageAttachments[0].url.openStream().readAllBytes().decodeToString()
        val rawConfig = try {
            RawConfig.readFromString(newConfig)
        } catch (e: IllegalStateException) {
            MessageUtil.sendEmbed(
                message.channel,
                MessageUtil.getEmbedBuilder()
                    .addField(
                        "An unexpected error occured. This is probably caused by a malformed config file. Perhaps this can help:",
                        "```$e: ${e.message}"
                    )
            )
            Log.log.info("Could not parse new config: $e: ${e.message}")
            return
        }
        try {
            ConfigParser.reloadLocalization(rawConfig)
            ConfigParser.reloadFeatures(rawConfig)
            ConfigParser.reloadCommands(rawConfig)
            ConfigParser.configFile.writeText(newConfig)
            message.channel.sendMessage("Config reloaded.")
        } catch (e: IllegalArgumentException) {
            MessageUtil.sendEmbed(
                message.channel,
                MessageUtil.getEmbedBuilder()
                    .addField("Error", "```${e.message}```")
            )
        }
    }
}
