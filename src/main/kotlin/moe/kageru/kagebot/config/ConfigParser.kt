package moe.kageru.kagebot.config

import moe.kageru.kagebot.Globals
import moe.kageru.kagebot.command.Command
import moe.kageru.kagebot.config.SystemSpec.serverId
import moe.kageru.kagebot.features.Features
import java.io.File

object ConfigParser {
    val configFile: File = File(RawConfig.DEFAULT_CONFIG_PATH)

    fun initialLoad(file: String) {
        val rawConfig = RawConfig.read(file)
        val config = Config.specs.file(RawConfig.getFile(file))
        Config.config = config

        Config.server = Globals.api.getServerById(config[serverId])
            .orElseThrow { IllegalArgumentException("Invalid server configured.") }
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
            ?: Features(RawFeatures(null, null))
    }
}

class Localization(
    val permissionDenied: String,
    val redirectedMessage: String,
    val messageDeleted: String,
    val timeout: String
) {

    constructor(rawLocalization: RawLocalization) : this(
        permissionDenied = rawLocalization.permissionDenied
            ?: throw IllegalArgumentException("No [localization.permissionDenied] defined"),
        redirectedMessage = rawLocalization.redirectedMessage
            ?: throw IllegalArgumentException("No [localization.redirectMessage] defined"),
        messageDeleted = rawLocalization.messageDeleted
            ?: throw IllegalArgumentException("No [localization.messageDeleted] defined"),
        timeout = rawLocalization.timeout
            ?: throw IllegalArgumentException("No [localization.timeout] defined")
    )
}
