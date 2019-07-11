package moe.kageru.kagebot.config

import com.google.gson.annotations.SerializedName


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
