package moe.kageru.kagebot.features

import moe.kageru.kagebot.Globals
import org.javacord.api.event.message.MessageCreateEvent

abstract class MessageFeature: Feature {
    abstract val commandEnabled: Boolean

    fun handle(message: MessageCreateEvent) {
        Globals.commandCounter.incrementAndGet()
        handleInternal(message)
    }

    abstract fun handleInternal(message: MessageCreateEvent)
}