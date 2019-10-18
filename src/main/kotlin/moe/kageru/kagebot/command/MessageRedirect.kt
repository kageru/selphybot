package moe.kageru.kagebot.command

import moe.kageru.kagebot.Log
import moe.kageru.kagebot.MessageUtil
import moe.kageru.kagebot.Util
import moe.kageru.kagebot.Util.applyIf
import moe.kageru.kagebot.Util.failed
import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.config.LocalizationSpec
import moe.kageru.kagebot.config.RawRedirect
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.event.message.MessageCreateEvent

internal class MessageRedirect(rawRedirect: RawRedirect) {
    private val target: TextChannel = rawRedirect.target?.let(Util::findChannel)
        ?: throw IllegalArgumentException("Every redirect needs to have a target.")
    private val anonymous: Boolean = rawRedirect.anonymous

    fun execute(message: MessageCreateEvent, command: Command) {
        val embed = MessageUtil.withEmbed {
            val redirectedText = message.readableMessageContent
                .applyIf(command.matchType == MatchType.PREFIX) { content ->
                    content.removePrefix(command.trigger).trim()
                }
            addField(Config.localization[LocalizationSpec.redirectedMessage], redirectedText)
            Log.info("Redirected message: $redirectedText")
        }
        // No inlined if/else because the types are different.
        // Passing the full message author will also include the avatar in the embed.
        embed.apply {
            if (anonymous) {
                setAuthor("Anonymous")
            } else {
                setAuthor(message.messageAuthor)
            }
        }

        if (MessageUtil.sendEmbed(target, embed).failed()) {
            target.sendMessage("Error: could not redirect message.")
            Log.warn("Could not redirect message to channel $target")
        }
    }
}
