package moe.kageru.kagebot.features

import moe.kageru.kagebot.Log
import moe.kageru.kagebot.MessageUtil
import moe.kageru.kagebot.Util
import moe.kageru.kagebot.Util.asOption
import moe.kageru.kagebot.Util.checked
import moe.kageru.kagebot.extensions.unwrap
import org.javacord.api.DiscordApi
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.event.server.member.ServerMemberJoinEvent

class WelcomeFeature(
  content: List<String>?,
  fallbackChannel: String?,
  private val fallbackMessage: String?,
) : MessageFeature, EventFeature {
  val embed: EmbedBuilder? by lazy { content?.let(MessageUtil::listToEmbed) }

  override fun register(api: DiscordApi) {
    api.addServerMemberJoinListener { event ->
      checked { welcomeUser(event) }
    }
  }

  fun welcomeUser(event: ServerMemberJoinEvent) {
    Log.info("User ${event.user.discriminatedName} joined")
    val message = event.user.sendMessage(embed)
    // If the user disabled direct messages, try the fallback (if defined)
    if (message.asOption().isEmpty() && hasFallback()) {
      fallbackChannel!!.sendMessage(
        fallbackMessage!!.replace("@@", event.user.mentionTag),
      )
    }
  }

  override fun handle(message: MessageCreateEvent) {
    embed?.let {
      MessageUtil.sendEmbed(message.channel, it)
    } ?: Log.info("Welcome command was triggered, but no welcome embed defined.")
  }

  private fun hasFallback(): Boolean = fallbackChannel != null && fallbackMessage != null

  private val fallbackChannel: TextChannel? = fallbackChannel?.let { channel ->
    requireNotNull(fallbackMessage) {
      "[feature.welcome.fallbackMessage] must not be null if fallbackChannel is defined"
    }
    Util.findChannel(channel).unwrap()
  }
}
