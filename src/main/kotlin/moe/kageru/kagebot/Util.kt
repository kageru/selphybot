package moe.kageru.kagebot

import arrow.core.Option
import arrow.core.extensions.list.foldable.find
import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.extensions.*
import moe.kageru.kagebot.config.Config.server
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.MessageAuthor
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.permission.Role
import org.javacord.api.entity.user.User
import org.javacord.api.event.message.MessageCreateEvent
import java.awt.Color
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException

object Util {
    inline fun <T> T.applyIf(condition: Boolean, op: (T) -> T): T {
        return if (condition) op(this) else this
    }

    /**
     * Mimics the behavior of [Optional.ifPresent], but returns null if the optional is empty,
     * allowing easier fallback behavior via Kotlin’s ?: operator.
     */
    internal inline fun <T, R> Optional<T>.ifNotEmpty(op: (T) -> R): R? =
        if (this.isPresent) op(this.get()) else null

    fun hasOneOf(messageAuthor: MessageAuthor, roles: Set<Role>): Boolean {
        return messageAuthor.asUser().asOption().flatMap { user ->
            user.roles().find { it in roles }
        }.nonEmpty()
    }

    private val channelIdRegex = Regex("\\d{18}")
    private fun String.isEntityId() = channelIdRegex.matches(this)

    @Throws(IllegalArgumentException::class)
    fun findRole(idOrName: String): Role {
        return when {
            idOrName.isEntityId() -> server.getRoleById(idOrName).ifNotEmpty { it }
                ?: throw IllegalArgumentException("Role $idOrName not found.")
            else -> server.getRolesByNameIgnoreCase(idOrName).getOnlyElementOrError(idOrName)
        }
    }

    private inline fun <reified T> List<T>.getOnlyElementOrError(identifier: String): T {
        val className = T::class.simpleName!!
        return when (size) {
            0 -> throw IllegalArgumentException("$className $identifier not found.")
            1 -> first()
            else -> throw IllegalArgumentException("More than one ${className.toLowerCase()} found with name $identifier. Please specify the role ID instead")
        }
    }

    private fun <T> Optional<T>.toNullable(): T? {
        return orElse(null)
    }

    fun findUser(idOrName: String): User? {
        return when {
            idOrName.isEntityId() -> server.getMemberById(idOrName).toNullable()
            else -> {
                when {
                    idOrName.contains('#') -> server.getMemberByDiscriminatedNameIgnoreCase(idOrName).toNullable()
                    else -> server.getMembersByName(idOrName).firstOrNull()
                }
            }
        }
    }

    fun <T> CompletableFuture<T>.failed(): Boolean {
        try {
            join()
        } catch (e: CompletionException) {
            // we don’t care about this error, but I at least want to log it for debugging
            Log.info(
                """Error during CompletableFuture:
                |$e
                |${e.localizedMessage}
                |${e.stackTrace.joinToString("\n\t")}
            """.trimMargin()
            )
        }
        return isCompletedExceptionally
    }

    @Throws(IllegalArgumentException::class)
    fun findChannel(idOrName: String): TextChannel {
        return when {
            idOrName.isEntityId() -> server.getTextChannelById(idOrName).ifNotEmpty { it }
                ?: throw IllegalArgumentException("Channel ID $idOrName not found.")
            else -> if (idOrName.startsWith('@')) {
                Globals.api.getCachedUserByDiscriminatedName(idOrName.removePrefix("@")).ifNotEmpty { user ->
                    user.openPrivateChannel().joinOr {
                        throw IllegalArgumentException("Could not open private channel with user $idOrName for redirection.")
                    }
                } ?: throw IllegalArgumentException("Can’t find user $idOrName for redirection.")
            } else {
                server.getTextChannelsByName(idOrName).getOnlyElementOrError(idOrName)
            }
        }
    }

    fun <T> Optional<T>.asOption(): Option<T> = if (this.isPresent) Option.just(this.get()) else Option.empty()

    inline fun checked(op: (() -> Unit)) {
        try {
            op()
        } catch (e: Exception) {
            Log.warn("An uncaught exception occurred.\n$e")
            Log.warn(e.stackTrace.joinToString("\n"))
            MessageUtil.sendEmbed(
                Globals.api.owner.get(),
                EmbedBuilder()
                    .setColor(Color.RED)
                    .addField("Error", "kagebot has encountered an error")
                    .addField(
                        "$e", """```
                       ${e.stackTrace.joinToString("\n")}
                    ```""".trimIndent().run { applyIf(length > 1800) { substring(1..1800) } }
                    )
            )
        }
    }

    fun MessageCreateEvent.getUser(): User? = Config.server.getMemberById(messageAuthor.id).toNullable()

    /**
     * Convert a list of elements to pairs, retaining order.
     * The last element is dropped if the input size is odd.
     * [1, 2, 3, 4, 5] -> [[1, 2], [3, 4]]
     */
    fun <T> Collection<T>.toPairs(): List<Pair<T, T>> = this.iterator().run {
        (0 until size / 2).map {
            Pair(next(), next())
        }
    }

    private inline fun <T> CompletableFuture<T>.joinOr(op: () -> Nothing): T {
        val value = join()
        if (isCompletedExceptionally) {
            op()
        }
        return value
    }
}
