package moe.kageru.kagebot

import org.javacord.api.entity.message.MessageAuthor
import java.util.*

object Util {
    inline fun <T> T.doIf(condition: (T) -> Boolean, op: (T) -> T): T {
        return if (condition(this)) op(this) else this
    }

    inline fun <T, R> Optional<T>.ifNotEmpty(op: (T) -> R): R? {
        if (this.isPresent) {
            return op(this.get())
        }
        return null
    }

    fun hasOneOf(messageAuthor: MessageAuthor, roles: Set<Long>): Boolean {
        return messageAuthor.asUser().ifNotEmpty { user ->
            user.getRoles(Config.server).map { it.id }.toSet().intersect(roles).isNotEmpty()
        } ?: false
    }
}