package moe.kageru.kagebot.command

import moe.kageru.kagebot.Globals
import moe.kageru.kagebot.Log.log
import moe.kageru.kagebot.MessageUtil
import moe.kageru.kagebot.Util
import moe.kageru.kagebot.config.RawRedirect
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.event.message.MessageCreateEvent

internal class MessageRedirect(rawRedirect: RawRedirect) {
    private val target: TextChannel = rawRedirect.target?.let(Util::findChannel)
        ?: throw IllegalArgumentException("Every redirect needs to have a target.")
    private val anonymous: Boolean = rawRedirect.anonymous

    fun execute(message: MessageCreateEvent, command: Command) {
        val embed = MessageUtil.getEmbedBuilder()
            .addField(
                Globals.localization.redirectedMessage,
                message.readableMessageContent.let { content ->
                    when (command.matchType) {
                        MatchType.PREFIX -> content.removePrefix(command.trigger).trim()
                        else -> content
                    }
                }
            )
        // No inlined if/else because the types are different.
        // Passing the full message author will also include the avatar in the embed.
        embed.apply {
            if (anonymous) {
                setAuthor("Anonymous")
            } else {
                setAuthor(message.messageAuthor)
            }
        }

        if (Util.wasSuccessful(target.sendMessage(embed))) {
            log.warning("Could not redirect message to channel $target")
        }
    }
}
