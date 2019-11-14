package moe.kageru.kagebot

import arrow.core.Either
import arrow.core.ListK
import arrow.core.Option
import arrow.core.extensions.either.monad.flatMap
import arrow.core.extensions.list.foldable.find
import arrow.core.firstOrNone
import moe.kageru.kagebot.config.Config.server
import moe.kageru.kagebot.extensions.*
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.MessageAuthor
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.permission.Role
import org.javacord.api.entity.user.User
import java.awt.Color
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException

object Util {
  inline fun <T> T.applyIf(condition: Boolean, op: (T) -> T): T {
    return if (condition) op(this) else this
  }

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

  fun findUser(idOrName: String): Option<User> {
    return when {
      idOrName.isEntityId() -> server.getMemberById(idOrName).asOption()
      idOrName.contains('#') -> server.getMemberByDiscriminatedNameIgnoreCase(idOrName).asOption()
      else -> server.membersByName(idOrName).firstOrNone()
    }
  }

  fun <T> CompletableFuture<T>.asOption(): Option<T> {
    return try {
      Option.just(join())
    } catch (e: CompletionException) {
      Option.empty()
    }
  }

  fun <T> Optional<T>.asOption(): Option<T> = if (this.isPresent) Option.just(this.get()) else Option.empty()

  fun findChannel(idOrName: String): Either<String, TextChannel> {
    return when {
      idOrName.isEntityId() -> server.channelById(idOrName).toEither { "Channel $idOrName not found" }
      idOrName.startsWith('@') -> Globals.api.getCachedUserByDiscriminatedName(idOrName.removePrefix("@")).asOption()
        .toEither { "User $idOrName not found" }
        .flatMap { user ->
          user.openPrivateChannel().asOption().toEither { "Can’t DM user $idOrName" }
        }
      else -> server.channelsByName(idOrName).getOnly().mapLeft { "Found $it channels for $idOrName, expected 1" }
    }
  }

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
                       ${e.stackTrace.joinToString("\n\t")}
                    ```""".trimIndent().run { applyIf(length > 1800) { substring(1..1800) } }
          )
      )
    }
  }
}
