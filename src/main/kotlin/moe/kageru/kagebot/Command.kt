package moe.kageru.kagebot

class Command(private val input: String, private val output: String) {
    fun matches(msg: String) = msg.startsWith(this.input)
    fun respond() = this.output
}