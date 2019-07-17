package moe.kageru.kagebot.config

import com.google.gson.annotations.SerializedName
import com.moandjiezana.toml.Toml
import java.io.File

class RawConfig(
    val system: RawSystemConfig?,
    val localization: RawLocalization?,
    @SerializedName("command")
    val commands: List<RawCommand>?,
    @SerializedName("feature")
    val features: RawFeatures?
) {
    companion object {
        const val DEFAULT_CONFIG_PATH = "config.toml"

        fun readFromString(tomlContent: String): RawConfig = Toml().read(tomlContent).to(RawConfig::class.java)

        private fun getFile(path: String): File {
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

class RawSystemConfig(val serverId: String?, val color: String?)
class RawLocalization(val permissionDenied: String?, val redirectedMessage: String?, val messageDeleted: String?)
