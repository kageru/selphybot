package moe.kageru.kagebot.features

import moe.kageru.kagebot.config.RawFeatures

class Features(val welcome: WelcomeFeature?) {

    constructor(rawFeatures: RawFeatures) : this(rawFeatures.welcome?.let(::WelcomeFeature))

    companion object {
        val NONE = Features(null)
    }
}
