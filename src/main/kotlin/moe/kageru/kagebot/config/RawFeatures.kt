package moe.kageru.kagebot.config

class RawFeatures(val welcome: RawWelcomeFeature?)
class RawWelcomeFeature(val content: Map<String, String>?, val fallbackChannel: String?, val fallbackMessage: String?)
