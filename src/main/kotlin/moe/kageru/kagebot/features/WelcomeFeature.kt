package moe.kageru.kagebot.features

import moe.kageru.kagebot.MessageUtil
import moe.kageru.kagebot.Util
import moe.kageru.kagebot.config.RawWelcomeFeature
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.event.message.MessageCreateEvent

class WelcomeFeature(rawWelcome: RawWelcomeFeature) : MessageFeature() {
    override fun handleInternal(message: MessageCreateEvent) {
        if (message.readableMessageContent == "!welcome") {
            message.channel.sendMessage(embed)
        }
    }

    val enabled: Boolean = rawWelcome.enabled
    val embed: EmbedBuilder? by lazy {
        rawWelcome.content?.let(MessageUtil::mapToEmbed)
    }
    val fallbackChannel: TextChannel? = rawWelcome.fallbackChannel?.let {
        if (rawWelcome.fallbackMessage == null) {
            throw IllegalArgumentException("[feature.welcome.fallbackMessage] must not be null if fallbackChannel is defined")
        }
        Util.findChannel(it)
    }
    val fallbackMessage: String? = rawWelcome.fallbackMessage
    val commandEnabled: Boolean = rawWelcome.commandEnabled
}