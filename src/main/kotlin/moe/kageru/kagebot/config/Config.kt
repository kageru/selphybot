package moe.kageru.kagebot.config

import moe.kageru.kagebot.Globals
import moe.kageru.kagebot.Globals.api
import moe.kageru.kagebot.command.Command
import moe.kageru.kagebot.features.Features
import java.awt.Color

class Config(rawConfig: RawConfig) {
    val system: SystemConfig = rawConfig.system?.let(::SystemConfig)
        ?: throw IllegalArgumentException("No [system] block in config.")
    var localization: Localization = rawConfig.localization?.let(::Localization)
        ?: throw IllegalArgumentException("No [localization] block in config.")
    var commands: List<Command>
    var features: Features

    init {
        Globals.server = api.getServerById(system.serverId).orElseThrow()
        this.commands = rawConfig.commands?.map(::Command) ?: emptyList()
        this.features = rawConfig.features?.let(::Features) ?: Features.NONE
    }

    fun reloadLocalization(rawLocalization: RawLocalization) {
        this.localization = Localization(rawLocalization)
    }

    fun reloadCommands(rawConfig: RawConfig) {
        this.commands = rawConfig.commands?.map(::Command)
            ?: throw IllegalArgumentException("No commands found in config.")
    }

    fun reloadFeatures(rawFeatures: RawFeatures) {
        this.features = Features(rawFeatures)
    }
}

class SystemConfig(val serverId: String, val color: Color) {
    constructor(rawSystemConfig: RawSystemConfig) : this(
        rawSystemConfig.serverId ?: throw IllegalArgumentException("No [system.server] defined."),
        Color.decode(rawSystemConfig.color ?: "#1793d0")
    )
}

class Localization(val permissionDenied: String, val redirectedMessage: String, val messageDeleted: String) {
    constructor(rawLocalization: RawLocalization) : this(
        permissionDenied = rawLocalization.permissionDenied
            ?: throw IllegalArgumentException("No [localization.permissionDenied] defined"),
        redirectedMessage = rawLocalization.redirectedMessage
            ?: throw IllegalArgumentException("No [localization.redirectMessage] defined"),
        messageDeleted = rawLocalization.messageDeleted
            ?: throw IllegalArgumentException("No [localization.messageDeleted] defined")
    )
}
