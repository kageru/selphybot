package moe.kageru.kagebot.config

import com.google.gson.annotations.SerializedName
import com.moandjiezana.toml.Toml
import com.uchuhimo.konf.ConfigSpec
import moe.kageru.kagebot.command.Command
import moe.kageru.kagebot.config.Config.system
import java.awt.Color
import java.io.File

class RawConfig(@SerializedName("feature") val features: RawFeatures?) {
    companion object {
        const val DEFAULT_CONFIG_PATH = "config.toml"

        fun readFromString(tomlContent: String): RawConfig = Toml().read(tomlContent).to(RawConfig::class.java)

        fun getFile(path: String): File {
            val file = File(path)
            if (file.isFile) {
                return file
            }
            println("Config not found, falling back to defaults...")
            return File(this::class.java.classLoader.getResource(path)!!.toURI())
        }

        fun read(path: String = DEFAULT_CONFIG_PATH): RawConfig {
            val toml: Toml = Toml().read(getFile(path))
            return toml.to(RawConfig::class.java)
        }
    }
}

object SystemSpec : ConfigSpec() {
    private val rawColor by optional("#1793d0", name = "color")
    val serverId by required<String>()
    val color by kotlin.lazy { Color.decode(system[rawColor])!! }
}

object LocalizationSpec : ConfigSpec() {
    val permissionDenied by optional("You do not have the permission to use this command.")
    val redirectedMessage by optional("says")
    val messageDeleted by optional("Your message was deleted.")
    val timeout by optional("You have been timed out for @@ minutes.")
}

object CommandSpec : ConfigSpec(prefix = "") {
    val command by optional(emptyList<Command>())
}
