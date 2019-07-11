package moe.kageru.kagebot.features

import moe.kageru.kagebot.config.RawFeatures

class Features(val welcome: WelcomeFeature?, val debug: DebugFeature?, val help: HelpFeature?) {
    constructor(rawFeatures: RawFeatures) : this(
        rawFeatures.welcome?.let(::WelcomeFeature),
        rawFeatures.debug?.let(::DebugFeature),
        rawFeatures.help?.let(::HelpFeature)
    )

    fun all() = listOfNotNull(this.welcome, this.debug, this.help)
    fun allWithMessage() = all().filterIsInstance<MessageFeature>()

    companion object {
        val NONE = Features(null, null, null)
    }
}

interface Feature