package moe.kageru.kagebot.features

import moe.kageru.kagebot.config.RawFeatures

class Features(
    val welcome: WelcomeFeature?,
    debug: DebugFeature,
    help: HelpFeature,
    getConfig: GetConfigFeature
) {
    constructor(rawFeatures: RawFeatures) : this(
        rawFeatures.welcome?.let(::WelcomeFeature),
        DebugFeature(),
        HelpFeature(),
        GetConfigFeature()
    )

    private val featureMap = mapOf(
        "help" to help,
        "debug" to debug,
        "welcome" to welcome,
        "getConfig" to getConfig
    )

    fun findByString(feature: String) = featureMap[feature]
}
