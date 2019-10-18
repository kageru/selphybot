package moe.kageru.kagebot.config

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.toml
import moe.kageru.kagebot.command.Command
import moe.kageru.kagebot.features.Features
import org.javacord.api.entity.server.Server

object Config {
    val systemSpec = Config { addSpec(SystemSpec) }.from.toml
    val localeSpec = Config { addSpec(LocalizationSpec) }.from.toml
    val commandSpec = Config { addSpec(CommandSpec) }.from.toml
    val featureSpec = Config { addSpec(FeatureSpec) }.from.toml

    lateinit var system: Config
    lateinit var localization: Config
    lateinit var commandConfig: Config
    lateinit var featureConfig: Config
    lateinit var server: Server

    // for easier access
    val features: Features get() = featureConfig[FeatureSpec.features]
    val commands: List<Command> get() = commandConfig[CommandSpec.command]
}
