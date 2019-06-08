package moe.kageru.kagebot

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class CommandTest : StringSpec({
    "should match prefix command" {
        testMessageSuccess("!ping", "pong")
    }
    "should match contains command" {
        testMessageSuccess("the trigger is somewhere in this message", "found it")
    }
    "should match regex command" {
        testMessageSuccess("AcsdB", "regex matched")
    }
    "should ping author" {
        testMessageSuccess("answer me", "<@1> there you go")
    }
    "should not react to own message" {
        val calls = mutableListOf<String>()
        Kagebot.processMessage(TestUtil.mockMessage("!ping", capturedCalls = calls, isSelf = true))
        calls.size shouldBe 0
    }
    "should delete messages" {
        val mockMessage = TestUtil.mockMessage("delet this")
        every { mockMessage.deleteMessage() } returns mockk()
        Kagebot.processMessage(mockMessage)
        verify(exactly = 1) { mockMessage.deleteMessage() }
    }
}) {
    companion object {
        fun testMessageSuccess(content: String, result: String) {
            val calls = mutableListOf<String>()
            Kagebot.processMessage(TestUtil.mockMessage(content, capturedCalls = calls))
            calls.size shouldBe 1
            calls[0] shouldBe result
        }
    }
}