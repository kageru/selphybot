package moe.kageru.kagebot.features

import moe.kageru.kagebot.config.RawFeatures

class Features(val welcome: WelcomeFeature?, val debug: DebugFeature?) {
    constructor(rawFeatures: RawFeatures) : this(
        rawFeatures.welcome?.let(::WelcomeFeature),
        rawFeatures.debug?.let(::DebugFeature)
    )

    fun all() = listOfNotNull(this.welcome, this.debug)
    fun allWithMessage() = listOfNotNull(this.welcome, this.debug).filterIsInstance<MessageFeature>()

    companion object {
        val NONE = Features(null, null)
    }
}

interface Feature