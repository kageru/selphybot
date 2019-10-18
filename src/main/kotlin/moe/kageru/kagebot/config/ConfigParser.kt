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
        val configFile = RawConfig.getFile(file)
        val config = Config.systemSpec.file(configFile)
        Config.system = config
        Config.server = Globals.api.getServerById(config[serverId])
            .orElseThrow { IllegalArgumentException("Invalid server configured.") }
        Config.localization = Config.localeSpec.file(configFile)
        reloadFeatures(rawConfig)
        reloadCommands(rawConfig)
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
