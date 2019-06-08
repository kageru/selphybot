package moe.kageru.kagebot

object Util {
    inline fun <T> T.doIf(condition: (T) -> Boolean, op: (T) -> T): T {
        return if (condition(this)) op(this) else this
    }
}