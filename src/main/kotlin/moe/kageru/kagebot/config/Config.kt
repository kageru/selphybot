package moe.kageru.kagebot.config

import moe.kageru.kagebot.Globals
import moe.kageru.kagebot.Globals.api
import moe.kageru.kagebot.command.Command
import moe.kageru.kagebot.features.Features
import java.awt.Color

class Config(rawConfig: RawConfig) {
    private val system: SystemConfig = rawConfig.system?.let(::SystemConfig)
        ?: throw IllegalArgumentException("No [system] block in config.")
    var localization: Localization = rawConfig.localization?.let(::Localization)
        ?: throw IllegalArgumentException("No [localization] block in config.")
    var features: Features

    init {
        Globals.systemConfig = system
        Globals.server = api.getServerById(system.serverId).orElseThrow()
        Globals.features = rawConfig.features?.let(::Features) ?: Features(RawFeatures(null))
        // TODO: remove this
        this.features = Globals.features
        Globals.commands = rawConfig.commands?.map(::Command) ?: emptyList()
        Globals.config = this
    }

    fun reloadLocalization(rawLocalization: RawLocalization) {
        this.localization = Localization(rawLocalization)
    }

    fun reloadCommands(rawConfig: RawConfig) {
        Globals.commands = rawConfig.commands?.map(::Command)?.toMutableList()
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
