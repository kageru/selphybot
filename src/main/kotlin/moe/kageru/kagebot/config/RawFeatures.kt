package moe.kageru.kagebot.config

class RawFeatures(val welcome: RawWelcomeFeature?, val timeout: RawTimeoutFeature?)
class RawWelcomeFeature(val content: List<String>?, val fallbackChannel: String?, val fallbackMessage: String?)
class RawTimeoutFeature(val role: String?)
