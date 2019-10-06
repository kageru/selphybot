package moe.kageru.kagebot.config

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.toml
import moe.kageru.kagebot.command.Command
import moe.kageru.kagebot.features.Features
import org.javacord.api.entity.server.Server

object Config {
    val specs = Config { addSpec(SystemSpec) }.from.toml
    lateinit var config: Config
    lateinit var server: Server
    lateinit var commands: List<Command>
    lateinit var features: Features
    lateinit var localization: Localization
}
