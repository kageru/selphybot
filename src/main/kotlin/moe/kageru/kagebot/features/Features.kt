package moe.kageru.kagebot.features

import moe.kageru.kagebot.MessageUtil
import moe.kageru.kagebot.Util
import moe.kageru.kagebot.config.RawFeatures
import moe.kageru.kagebot.config.RawWelcomeFeature
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.embed.EmbedBuilder

class Features(rawFeatures: RawFeatures) {
    val welcome: WelcomeFeature? = rawFeatures.welcome?.let { WelcomeFeature(it) }
}

class WelcomeFeature(rawWelcome: RawWelcomeFeature) {
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

}
