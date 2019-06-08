package moe.kageru.kagebot

import com.moandjiezana.toml.Toml
import org.javacord.api.entity.server.Server
import java.io.File

class Config(val system: System, val localization: Localization, val commands: List<Command>) {
    companion object {
        val config: Config by lazy { read("config.toml") }
        val secret = File("secret").readText().replace("\n", "")
        var server: Server? = null
            get() = field!!

        private fun read(path: String): Config {
            val rawConfig: Toml = Toml().read(run {
                val file = File(path)
                if (file.isFile) {
                    return@run file
                }
                println("Config not found, falling back to defaults...")
                File(this::class.java.classLoader.getResource(path)!!.toURI())
            })
            val parsed = rawConfig.to(Config::class.java)
            return Config(
                parsed.system,
                parsed.localization,
                parsed.commands.map { Command(it) }
            )
        }

    }
}

data class System(val serverId: String)
data class Localization(val permissionDenied: String)