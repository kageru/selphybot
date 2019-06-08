package moe.kageru.kagebot

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class CommandTest : StringSpec({
    "should match prefix command" {
        testMessage("!ping", "pong")
    }
    "should match contains command" {
        testMessage("the trigger is somewhere in this message", "found it")
    }
    "should match regex command" {
        testMessage("AcsdB", "regex matched")
    }
    "should ping author" {
        testMessage("answer me", "<@1> there you go")
    }
}) {
    companion object {
        fun testMessage(content: String, result: String) {
            val calls = mutableListOf<String>()
            Kagebot.processMessage(TestUtil.mockMessage(content, capturedCalls = calls))
            calls.size shouldBe 1
            calls[0] shouldBe result
        }
    }
}