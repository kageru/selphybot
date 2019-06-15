package moe.kageru.kagebot.features

import moe.kageru.kagebot.config.RawFeatures

class Features(val welcome: WelcomeFeature?, val debug: DebugFeatures?) {

    constructor(rawFeatures: RawFeatures) : this(
        rawFeatures.welcome?.let(::WelcomeFeature),
        rawFeatures.debug?.let(::DebugFeatures)
    )

    companion object {
        val NONE = Features(null, null)
    }
}
