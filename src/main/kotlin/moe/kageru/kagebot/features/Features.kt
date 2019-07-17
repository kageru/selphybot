package moe.kageru.kagebot.features

import moe.kageru.kagebot.config.RawFeatures

class Features(
    val welcome: WelcomeFeature?,
    debug: DebugFeature,
    help: HelpFeature,
    getConfig: GetConfigFeature,
    setConfig: SetConfigFeature
) {
    constructor(rawFeatures: RawFeatures) : this(
        rawFeatures.welcome?.let(::WelcomeFeature),
        DebugFeature(),
        HelpFeature(),
        GetConfigFeature(),
        SetConfigFeature()
    )

    private val all = listOf(welcome, debug, help, getConfig, setConfig)
    private val featureMap = mapOf(
        "help" to help,
        "debug" to debug,
        "welcome" to welcome,
        "getConfig" to getConfig,
        "setConfig" to setConfig
    )

    fun findByString(feature: String) = featureMap[feature]
    fun eventFeatures() = all.filterIsInstance<EventFeature>()
}
