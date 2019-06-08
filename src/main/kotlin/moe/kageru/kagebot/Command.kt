package moe.kageru.kagebot

class Command(trigger: String?, response: String?, matchType: MatchType?) {
    val trigger: String = trigger!!
    val regex: Regex? = if (matchType == MatchType.REGEX) Regex.fromLiteral(trigger!!) else null
    private val response: String = response!!
    private val matchType: MatchType = matchType ?: MatchType.PREFIX

    constructor(cmd: Command) : this(cmd.trigger, cmd.response, cmd.matchType)

    fun matches(msg: String) = this.matchType.matches(msg, this)
    fun respond() = this.response
}

enum class MatchType {
    PREFIX {
        override fun matches(message: String, command: Command) = message.startsWith(command.trigger)
    },
    FULL {
        override fun matches(message: String, command: Command) = message == command.trigger
    },
    CONTAINS {
        override fun matches(message: String, command: Command) = message.contains(command.trigger)
    },
    REGEX {
        override fun matches(message: String, command: Command) = command.regex!!.matches(message)
    };

    abstract fun matches(message: String, command: Command): Boolean
}
