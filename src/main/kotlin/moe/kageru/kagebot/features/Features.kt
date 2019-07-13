package moe.kageru.kagebot.features

import moe.kageru.kagebot.config.RawFeatures

class Features(val welcome: WelcomeFeature?, debug: DebugFeature, help: HelpFeature) {
    constructor(rawFeatures: RawFeatures) : this(
        rawFeatures.welcome?.let(::WelcomeFeature),
        DebugFeature(),
        HelpFeature()
    )

    private val featureMap = mapOf("help" to help, "debug" to debug, "welcome" to welcome)

    fun findByString(feature: String) = featureMap[feature]
}
