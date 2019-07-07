package moe.kageru.kagebot

import moe.kageru.kagebot.command.Command
import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.config.SystemConfig
import org.javacord.api.DiscordApi
import org.javacord.api.entity.server.Server
import java.util.concurrent.atomic.AtomicInteger

object Globals {
    lateinit var server: Server
    lateinit var api: DiscordApi
    lateinit var config: Config
    lateinit var commands: List<Command>
    lateinit var systemConfig: SystemConfig
    var commandCounter: AtomicInteger = AtomicInteger(0)
}