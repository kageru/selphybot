package moe.kageru.kagebot.config

import moe.kageru.kagebot.Globals
import moe.kageru.kagebot.command.Command
import moe.kageru.kagebot.features.Features
import java.awt.Color
import java.io.File

object ConfigParser {
    val configFile: File = File(RawConfig.DEFAULT_CONFIG_PATH)

    fun initialLoad(rawConfig: RawConfig) {
        val systemConfig = rawConfig.system?.let(::SystemConfig)
            ?: throw IllegalArgumentException("No [system] block in config.")
        Config.server = Globals.api.getServerById(systemConfig.serverId).orElseThrow { IllegalArgumentException("Invalid server configured.") }
        Config.systemConfig = systemConfig
        reloadLocalization(rawConfig)
        reloadFeatures(rawConfig)
        reloadCommands(rawConfig)
    }

    fun reloadLocalization(rawConfig: RawConfig) {
        Config.localization = rawConfig.localization?.let(::Localization)
            ?: throw IllegalArgumentException("No [localization] block in config.")
    }

    fun reloadCommands(rawConfig: RawConfig) {
        Config.commands = rawConfig.commands?.map(::Command)?.toMutableList()
            ?: throw IllegalArgumentException("No commands found in config.")
    }

    fun reloadFeatures(rawConfig: RawConfig) {
        Config.features = rawConfig.features?.let(::Features)
            ?: Features(RawFeatures(null))
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
