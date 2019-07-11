package moe.kageru.kagebot

import moe.kageru.kagebot.command.Command
import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.config.SystemConfig
import moe.kageru.kagebot.features.Features
import org.javacord.api.DiscordApi
import org.javacord.api.entity.server.Server
import java.util.concurrent.atomic.AtomicInteger

object Globals {
    lateinit var server: Server
    lateinit var api: DiscordApi
    lateinit var config: Config
    lateinit var commands: List<Command>
    lateinit var systemConfig: SystemConfig
    lateinit var features: Features
    var commandCounter: AtomicInteger = AtomicInteger(0)
}