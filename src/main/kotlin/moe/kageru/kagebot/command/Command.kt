package moe.kageru.kagebot.command

import com.fasterxml.jackson.annotation.JsonProperty
import moe.kageru.kagebot.Globals
import moe.kageru.kagebot.Log
import moe.kageru.kagebot.MessageUtil
import moe.kageru.kagebot.Util.applyIf
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
        response.applyIf(response.contains(AUTHOR_PLACEHOLDER)) {
            it.replace(AUTHOR_PLACEHOLDER, MessageUtil.mention(author))
        }
}

@Suppress("unused")
enum class MatchType {
    PREFIX {
        override fun matches(message: String, command: Command) = message.startsWith(command.trigger, ignoreCase = true)
    },
    CONTAINS {
        override fun matches(message: String, command: Command) = message.contains(command.trigger, ignoreCase = true)
    },
    REGEX {
        override fun matches(message: String, command: Command) = command.regex!!.matches(message)
    };

    abstract fun matches(message: String, command: Command): Boolean
}
