package moe.kageru.kagebot.config

import moe.kageru.kagebot.Globals
import moe.kageru.kagebot.Globals.api
import moe.kageru.kagebot.command.Command
import moe.kageru.kagebot.features.Features
import moe.kageru.kagebot.features.WelcomeFeature
import java.awt.Color
import kotlin.IllegalArgumentException

class Config(rawConfig: RawConfig) {
    val system: SystemConfig
    val localization: Localization
    val commands: List<Command>
    val features: Features

    init {
        system = rawConfig.system?.let {
            SystemConfig(
                it.serverId ?: throw IllegalArgumentException("No [system.server] defined."),
                Color.decode(it.color ?: "#1793d0")
            )
        } ?: throw IllegalArgumentException("No [system] block in config.")
        Globals.server = api.getServerById(system.serverId).orElseThrow()

        localization = rawConfig.localization?.let {
            Localization(
                permissionDenied = it.permissionDenied
                    ?: throw IllegalArgumentException("No [localization.permissionDenied] defined"),
                redirectedMessage = it.redirectedMessage
                    ?: throw IllegalArgumentException("No [localization.permissionDenied] defined"),
                messageDeleted = it.messageDeleted
                    ?: throw IllegalArgumentException("No [localization.permissionDenied] defined")
            )
        } ?: throw IllegalArgumentException("No [localization] block in config.")

        commands = rawConfig.commands?.let { rawCommands ->
            rawCommands.map { Command(it) }
        } ?: emptyList()

        features = rawConfig.features?.let { Features(it) }
            ?: throw IllegalArgumentException("No [feature] block in config.")
    }
}

class SystemConfig(val serverId: String, val color: Color)
class Localization(val permissionDenied: String, val redirectedMessage: String, val messageDeleted: String)
