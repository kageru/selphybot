package moe.kageru.kagebot

import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import moe.kageru.kagebot.Config.Companion.config
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.core.entity.message.embed.EmbedBuilderDelegateImpl

class CommandTest : StringSpec({
    TestUtil.prepareServerConfig()
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
    "should refuse command without permissions" {
        val calls = mutableListOf<String>()
        val mockMessage = TestUtil.mockMessage("!restricted", capturedCalls = calls)
        every { mockMessage.messageAuthor.asUser() } returns mockk {
            every { get().getRoles(any()) } returns emptyList()
            every { isPresent } returns true
        }
        Kagebot.processMessage(mockMessage)
        calls.size shouldBe 1
        calls[0] shouldBe config.localization.permissionDenied
    }
    "should accept restricted command for owner" {
        val calls = mutableListOf<String>()
        val mockMessage = TestUtil.mockMessage("!restricted", capturedCalls = calls)
        every { mockMessage.messageAuthor.isBotOwner } returns true
        Kagebot.processMessage(mockMessage)
        calls.size shouldBe 1
        calls[0] shouldBe "access granted"
    }
    "should accept restricted command with permissions" {
        val calls = mutableListOf<String>()
        val mockMessage = TestUtil.mockMessage("!restricted", capturedCalls = calls)
        every { mockMessage.messageAuthor.asUser() } returns mockk {
            every { isPresent } returns true
            every { get().getRoles(any()) } returns listOf(
                mockk { every { id } returns 452034011393425409 }
            )
        }
        Kagebot.processMessage(mockMessage)
        calls.size shouldBe 1
        calls[0] shouldBe "access granted"
    }
    /*
     * This implicitly tests that the message author is not included in anonymous complaints
     * because getting the author’s name from the mock is undefined.
     */
    "should redirect" {
        val calls = mutableListOf<EmbedBuilder>()
        TestUtil.prepareServerConfig(calls)
        val message = "this is a message"
        Kagebot.processMessage(TestUtil.mockMessage("!anonRedirect $message"))
        calls.size shouldBe 1
        val delegateImpl = calls[0].delegate as EmbedBuilderDelegateImpl
        val embedContent = delegateImpl.toJsonNode().toString()
        embedContent shouldContain "\"$message\""

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