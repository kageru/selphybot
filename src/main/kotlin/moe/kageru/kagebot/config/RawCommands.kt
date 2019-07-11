package moe.kageru.kagebot.config

import com.google.gson.annotations.SerializedName

class RawCommand(
    val trigger: String?,
    val response: String?,
    val matchType: String?,
    val permissions: RawPermissions?,
    @SerializedName("action") val actions: RawMessageActions?,
    val embed: Map<String, String>?,
    val feature: String?
)

class RawPermissions(val hasOneOf: List<String>?, val hasNoneOf: List<String>?, val onlyDM: Boolean)
class RawMessageActions(val delete: Boolean, val redirect: RawRedirect?)
class RawRedirect(val target: String?, val anonymous: Boolean)