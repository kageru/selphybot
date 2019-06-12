package moe.kageru.kagebot

import moe.kageru.kagebot.Globals.api
import moe.kageru.kagebot.Globals.server
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.MessageAuthor
import org.javacord.api.entity.permission.Role
import java.util.*

object Util {
    inline fun <T> T.doIf(condition: (T) -> Boolean, op: (T) -> T): T {
        return if (condition(this)) op(this) else this
    }

    /**
     * Mimics the behavior of [Optional.ifPresent], but returns null if the optional is empty,
     * allowing easier fallback behavior via Kotlin’s ?: operator.
     */
    inline fun <T, R> Optional<T>.ifNotEmpty(op: (T) -> R): R? {
        if (this.isPresent) {
            return op(this.get())
        }
        return null
    }

    fun hasOneOf(messageAuthor: MessageAuthor, roles: Set<Role>): Boolean {
        return messageAuthor.asUser().ifNotEmpty { user ->
            user.getRoles(server).map { it }.toSet().intersect(roles).isNotEmpty()
        } ?: false
    }

    private val channelIdRegex = Regex("\\d{18}")
    fun String.isEntityId() = channelIdRegex.matches(this)

    @Throws(IllegalArgumentException::class)
    fun findRole(idOrName: String): Role {
        return when {
            idOrName.isEntityId() -> server.getRoleById(idOrName).ifNotEmpty { it }
                ?: throw IllegalArgumentException("Role $idOrName not found.")
            else -> server.getRolesByNameIgnoreCase(idOrName).let {
                when (it.size) {
                    0 -> throw IllegalArgumentException("Role $idOrName not found.")
                    1 -> it[0]
                    else -> throw IllegalArgumentException("More than one role found with name $idOrName. Please specify the role ID instead")
                }
            }
        }
    }

    @Throws(IllegalArgumentException::class)
    fun findChannel(idOrName: String): TextChannel {
        return when {
            idOrName.isEntityId() -> server.getTextChannelById(idOrName).ifNotEmpty { it }
                ?: throw IllegalArgumentException("Channel ID $idOrName not found.")
            else -> if (idOrName.startsWith('@')) {
                api.getCachedUserByDiscriminatedName(idOrName.removePrefix("@")).ifNotEmpty { user ->
                    user.privateChannel.ifNotEmpty { it }
                        ?: throw IllegalArgumentException("Could not open private channel with user $idOrName for redirection.")
                }
                    ?: throw IllegalArgumentException("Can’t find user $idOrName for redirection.")
            } else {
                server.getTextChannelsByName(idOrName).let {
                    when (it.size) {
                        0 -> throw IllegalArgumentException("Channel $idOrName not found.")
                        1 -> it[0]
                        else -> throw IllegalArgumentException("More than one channel found with name $idOrName. Please specify the channel ID instead")
                    }
                }
            }
        }
    }
}