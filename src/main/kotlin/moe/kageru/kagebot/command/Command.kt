package moe.kageru.kagebot.command

import com.fasterxml.jackson.annotation.JsonProperty
import moe.kageru.kagebot.Globals
import moe.kageru.kagebot.Log
import moe.kageru.kagebot.MessageUtil
import moe.kageru.kagebot.MessageUtil.mention
import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.features.MessageFeature
import org.javacord.api.entity.message.MessageAuthor
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.event.message.MessageCreateEvent

private const val AUTHOR_PLACEHOLDER = "@@"

class Command(
    val trigger: String,
    private val response: String? = null,
    private val permissions: Permissions?,
    @JsonProperty("action")
    private val actions: MessageActions?,
    embed: List<String>?,
    feature: String?,
    matchType: String?
) {
    val matchType: MatchType = matchType?.let { type ->
        MatchType.values().find { it.name.equals(type, ignoreCase = true) }
            ?: throw IllegalArgumentException("Invalid [command.matchType]: “$matchType”")
    } ?: MatchType.PREFIX
    val regex: Regex? = if (this.matchType == MatchType.REGEX) Regex(trigger) else null
    val embed: EmbedBuilder? = embed?.let(MessageUtil::listToEmbed)
    private val feature: MessageFeature? = feature?.let { Config.features.findByString(it) }

    fun matches(msg: String) = this.matchType.matches(msg, this)

    fun isAllowed(message: MessageCreateEvent) = permissions?.isAllowed(message) ?: true

    fun execute(message: MessageCreateEvent) {
        Log.info("Executing command ${this.trigger} triggered by user ${message.messageAuthor.discriminatedName} (ID: ${message.messageAuthor.id})")
        Globals.commandCounter.incrementAndGet()
        this.actions?.run(message, this)
        this.response?.let {
            message.channel.sendMessage(respond(message.messageAuthor, it))
        }
        this.embed?.let {
            MessageUtil.sendEmbed(message.channel, embed)
        }
        this.feature?.handle(message)
    }

    private fun respond(author: MessageAuthor, response: String) =
        response.replace(AUTHOR_PLACEHOLDER, author.mention())
}

@Suppress("unused")
enum class MatchType(val matches: (String, Command) -> Boolean) {
    PREFIX({ message, command -> message.startsWith(command.trigger, ignoreCase = true) }),
    CONTAINS({ message, command -> message.contains(command.trigger, ignoreCase = true) }),
    REGEX({ message, command -> command.regex!!.matches(message) });
}
