package moe.kageru.kagebot

import org.javacord.api.DiscordApi
import java.util.concurrent.atomic.AtomicInteger

object Globals {
    lateinit var api: DiscordApi
    val commandCounter: AtomicInteger = AtomicInteger(0)
}
