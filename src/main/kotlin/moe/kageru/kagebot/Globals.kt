package moe.kageru.kagebot

import moe.kageru.kagebot.config.Config
import org.javacord.api.DiscordApi
import org.javacord.api.entity.server.Server
import java.util.concurrent.atomic.AtomicInteger

object Globals {
    lateinit var server: Server
    lateinit var api: DiscordApi
    lateinit var config: Config
    var commandCounter: AtomicInteger = AtomicInteger(0)
}