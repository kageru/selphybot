package moe.kageru.kagebot.features

import moe.kageru.kagebot.Globals
import moe.kageru.kagebot.MessageUtil
import moe.kageru.kagebot.command.MatchType
import org.javacord.api.event.message.MessageCreateEvent

class HelpFeature : MessageFeature() {
    override fun handleInternal(message: MessageCreateEvent) {
        message.channel.sendMessage(
            MessageUtil.getEmbedBuilder()
                .addField("Commands:", listCommands(message))
        )
    }
}

private fun listCommands(message: MessageCreateEvent) = Globals.commands
    .filter { it.matchType == MatchType.PREFIX && it.isAllowed(message) }
    .map { it.trigger }
    .joinToString("\n")