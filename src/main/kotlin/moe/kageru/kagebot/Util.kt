package moe.kageru.kagebot

import arrow.core.*
import arrow.core.extensions.either.monad.flatMap
import arrow.core.extensions.list.foldable.find
import arrow.typeclasses.Monad
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

    fun findRole(idOrName: String): Either<String, Role> {
        return when {
            idOrName.isEntityId() -> server.getRoleById(idOrName).asOption().toEither { 0 }
            else -> server.rolesByName(idOrName).getOnly()
        }.mapLeft { "Found $it results, expected 1" }
    }

    private fun <T> ListK<T>.getOnly(): Either<Int, T> {
        return when (size) {
            1 -> Either.right(first())
            else -> Either.left(size)
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

    fun <T> CompletableFuture<T>.asOption(): Option<T> {
        return try {
            Option.just(join())
        } catch (e: CompletionException) {
            Option.empty()
        }
    }

    fun <T> Either<*, T>.unwrap(): T = this.getOrElse { error("Attempted to unwrap Either.left") }

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

    fun findChannel(idOrName: String): Either<String, TextChannel> {
        return when {
            idOrName.isEntityId() -> server.getTextChannelById(idOrName).asOption().toEither { "Channel $idOrName not found" }
            idOrName.startsWith('@') -> Globals.api.getCachedUserByDiscriminatedName(idOrName.removePrefix("@")).asOption()
                .toEither { "User $idOrName not found" }
                .flatMap { user ->
                    user.openPrivateChannel().asOption().toEither { "Can’t DM user $idOrName" }
                }
            else -> server.channelByName(idOrName).getOnly().mapLeft { "Found $it channels for $idOrName, expected 1" }
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
