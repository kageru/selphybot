package moe.kageru.kagebot.config

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.toml
import moe.kageru.kagebot.command.Command
import moe.kageru.kagebot.features.Features
import org.javacord.api.entity.server.Server

object Config {
    val systemSpec = Config { addSpec(SystemSpec) }.from.toml
    val localeSpec = Config { addSpec(LocalizationSpec) }.from.toml
    lateinit var system: Config
    lateinit var localization: Config
    lateinit var server: Server
    lateinit var commands: List<Command>
    lateinit var features: Features
}
