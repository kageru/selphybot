package moe.kageru.kagebot.features

import moe.kageru.kagebot.config.RawFeatures

class Features(
    val welcome: WelcomeFeature?,
    debug: DebugFeature,
    help: HelpFeature,
    getConfig: GetConfigFeature,
    setConfig: SetConfigFeature,
    val timeout: TimeoutFeature?
) {
    constructor(rawFeatures: RawFeatures) : this(
        rawFeatures.welcome?.let(::WelcomeFeature),
        DebugFeature(),
        HelpFeature(),
        GetConfigFeature(),
        SetConfigFeature(),
        rawFeatures.timeout?.let(::TimeoutFeature)
    )

    private val all = listOf(welcome, debug, help, getConfig, setConfig, timeout)
    private val featureMap = mapOf(
        "help" to help,
        "debug" to debug,
        "welcome" to welcome,
        "getConfig" to getConfig,
        "setConfig" to setConfig,
        "timeout" to timeout
    )

    fun findByString(feature: String) = featureMap[feature]
    fun eventFeatures() = all.filterIsInstance<EventFeature>()
}
