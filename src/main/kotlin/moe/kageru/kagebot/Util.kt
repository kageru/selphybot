package moe.kageru.kagebot

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
}