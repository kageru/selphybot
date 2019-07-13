package moe.kageru.kagebot.features

import org.javacord.api.event.message.MessageCreateEvent

abstract class MessageFeature {
    fun handle(message: MessageCreateEvent) {
        handleInternal(message)
    }

    internal abstract fun handleInternal(message: MessageCreateEvent)
}
