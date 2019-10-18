package moe.kageru.kagebot.features

import moe.kageru.kagebot.Log
import moe.kageru.kagebot.MessageUtil
import moe.kageru.kagebot.Util
import moe.kageru.kagebot.Util.checked
import moe.kageru.kagebot.Util.failed
import moe.kageru.kagebot.config.RawWelcomeFeature
import org.javacord.api.DiscordApi
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.event.server.member.ServerMemberJoinEvent

class WelcomeFeature(rawWelcome: RawWelcomeFeature) : MessageFeature, EventFeature {
    override fun register(api: DiscordApi) {
        api.addServerMemberJoinListener { event ->
            checked { welcomeUser(event) }
        }
    }

    fun welcomeUser(event: ServerMemberJoinEvent) {
        Log.info("User ${event.user.discriminatedName} joined")
        val message = event.user.sendMessage(embed)
        // If the user disabled direct messages, try the fallback (if defined)
        if (message.failed() && hasFallback()) {
            fallbackChannel!!.sendMessage(
                fallbackMessage!!.replace("@@", MessageUtil.mention(event.user))
            )
        }
    }

    override fun handle(message: MessageCreateEvent) {
        embed?.let {
            val sent = MessageUtil.sendEmbed(message.channel, it)
            // invoke this for logging
            sent.failed()
        } ?: Log.info("Welcome command was triggered, but no welcome embed defined.")
    }

    private fun hasFallback(): Boolean = fallbackChannel != null && fallbackMessage != null

    val embed: EmbedBuilder? by lazy {
        rawWelcome.content?.let(MessageUtil::listToEmbed)
    }
    private val fallbackChannel: TextChannel? = rawWelcome.fallbackChannel?.let {
        requireNotNull(rawWelcome.fallbackMessage) {
            "[feature.welcome.fallbackMessage] must not be null if fallbackChannel is defined"
        }
        Util.findChannel(it)
    }
    private val fallbackMessage: String? = rawWelcome.fallbackMessage
}
