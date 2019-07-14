package moe.kageru.kagebot.features

import moe.kageru.kagebot.MessageUtil
import moe.kageru.kagebot.command.MatchType
import moe.kageru.kagebot.config.Config
import org.javacord.api.event.message.MessageCreateEvent

class HelpFeature : MessageFeature() {
    override fun handleInternal(message: MessageCreateEvent) {
        MessageUtil.sendEmbed(
            message.channel,
            MessageUtil.getEmbedBuilder()
                .addField("Commands:", listCommands(message))
        )
    }
}

private fun listCommands(message: MessageCreateEvent) = Config.commands
    .filter { it.matchType == MatchType.PREFIX && it.isAllowed(message) }
    .map { it.trigger }
    .joinToString("\n")
