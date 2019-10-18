package moe.kageru.kagebot.features

class Features(val welcome: WelcomeFeature?, val timeout: TimeoutFeature?) {
    private val debug = DebugFeature()
    private val help = HelpFeature()
    private val getConfig = GetConfigFeature()
    private val setConfig = SetConfigFeature()

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

    companion object {
        val DEFAULT = Features(null, null)
    }
}
