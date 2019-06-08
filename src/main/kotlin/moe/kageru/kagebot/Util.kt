package moe.kageru.kagebot

import org.javacord.api.event.message.MessageCreateEvent

object Util {
    inline fun <T> T.doIf(condition: (T) -> Boolean, op: (T) -> T): T {
        return if (condition(this)) op(this) else this
    }

    fun MessageCreateEvent.asString(): String =
        "<${this.messageAuthor.discriminatedName}> ${this.readableMessageContent}"

}