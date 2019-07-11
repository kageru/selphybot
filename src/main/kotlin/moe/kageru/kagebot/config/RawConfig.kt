package moe.kageru.kagebot.config

import com.google.gson.annotations.SerializedName
import com.moandjiezana.toml.Toml
import java.io.File

class RawConfig(
    val system: RawSystemConfig?,
    val localization: RawLocalization?,
    @SerializedName("command") val commands: List<RawCommand>?,
    @SerializedName("feature") val features: RawFeatures?
) {
    companion object {
        const val DEFAULT_CONFIG_PATH = "config.toml"

        fun readFromString(tomlContent: String) = Toml().read(tomlContent).to(RawConfig::class.java)

        fun read(path: String = DEFAULT_CONFIG_PATH): RawConfig {
            val toml: Toml = Toml().read(run {
                val file = File(path)
                if (file.isFile) {
                    return@run file
                }
                println("Config not found, falling back to defaults...")
                File(this::class.java.classLoader.getResource(path)!!.toURI())
            })
            return toml.to(RawConfig::class.java)
        }
    }
}

class RawSystemConfig(val serverId: String?, val color: String?)
class RawLocalization(val permissionDenied: String?, val redirectedMessage: String?, val messageDeleted: String?)
class RawCommand(
    val trigger: String?,
    val response: String?,
    val matchType: String?,
    val permissions: RawPermissions?,
    @SerializedName("action") val actions: RawMessageActions?,
    val embed: Map<String, String>?
)

class RawPermissions(val hasOneOf: List<String>?, val hasNoneOf: List<String>?, val onlyDM: Boolean)
class RawMessageActions(val delete: Boolean, val redirect: RawRedirect?)
class RawRedirect(val target: String?, val anonymous: Boolean)
class RawFeatures(val welcome: RawWelcomeFeature?, val debug: RawDebugFeature?, val help: RawHelpFeature?)
class RawWelcomeFeature(
    val enable: Boolean,
    val content: Map<String, String>?,
    val fallbackChannel: String?,
    val fallbackMessage: String?,
    @SerializedName("command") val commandEnabled: Boolean
)
class RawDebugFeature(val enable: Boolean)
class RawHelpFeature(val enable: Boolean)