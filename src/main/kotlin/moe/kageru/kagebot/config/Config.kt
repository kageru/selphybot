package moe.kageru.kagebot.config

import moe.kageru.kagebot.command.Command
import moe.kageru.kagebot.features.Features
import org.javacord.api.entity.server.Server

object Config {
    lateinit var server: Server
    lateinit var commands: List<Command>
    lateinit var systemConfig: SystemConfig
    lateinit var features: Features
    lateinit var localization: Localization
}
