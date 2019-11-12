package moe.kageru.kagebot.command

import moe.kageru.kagebot.Log
import moe.kageru.kagebot.MessageUtil
import moe.kageru.kagebot.Util
import moe.kageru.kagebot.Util.applyIf
import moe.kageru.kagebot.Util.failed
import moe.kageru.kagebot.Util.unwrap
import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.config.LocalizationSpec
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.event.message.MessageCreateEvent

class MessageRedirect(target: String, private val anonymous: Boolean = false) {
    private val targetChannel: TextChannel = Util.findChannel(target).unwrap()

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

        if (MessageUtil.sendEmbed(targetChannel, embed).failed()) {
            targetChannel.sendMessage("Error: could not redirect message.")
            Log.warn("Could not redirect message to channel $targetChannel")
        }
    }
}
