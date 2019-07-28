package moe.kageru.kagebot.features

import org.javacord.api.DiscordApi
import org.javacord.api.event.message.MessageCreateEvent

interface MessageFeature {
    fun handle(message: MessageCreateEvent)
}

interface EventFeature {
    fun register(api: DiscordApi)
}
