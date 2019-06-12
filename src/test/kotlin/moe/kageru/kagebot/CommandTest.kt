package moe.kageru.kagebot

import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import moe.kageru.kagebot.TestUtil.embedToString
import moe.kageru.kagebot.TestUtil.messageableAuthor
import moe.kageru.kagebot.TestUtil.mockMessage
import moe.kageru.kagebot.TestUtil.testMessageSuccess
import moe.kageru.kagebot.config.RawConfig.Companion.config
import org.javacord.api.entity.message.embed.EmbedBuilder

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
        Kagebot.processMessage(mockMessage("!ping", replies = calls, isSelf = true))
        calls shouldBe mutableListOf()
    }
    "should delete messages and send copy to author" {
        val replies = mutableListOf<EmbedBuilder>()
        val messageContent = "delet this"
        val mockMessage = mockMessage(messageContent)
        every { mockMessage.deleteMessage() } returns mockk()
        every { mockMessage.messageAuthor.asUser() } returns messageableAuthor(replies)
        Kagebot.processMessage(mockMessage)
        verify(exactly = 1) { mockMessage.deleteMessage() }
        replies.size shouldBe 1
        embedToString(replies[0]) shouldContain messageContent
    }
    "should refuse command without permissions" {
        val replies = mutableListOf<String>()
        val mockMessage = mockMessage("!restricted", replies = replies)
        every { mockMessage.messageAuthor.asUser() } returns messageableAuthor()
        Kagebot.processMessage(mockMessage)
        replies shouldBe mutableListOf(config.localization.permissionDenied)
    }
    "should accept restricted command for owner" {
        val calls = mutableListOf<String>()
        val mockMessage = mockMessage("!restricted", replies = calls)
        every { mockMessage.messageAuthor.isBotOwner } returns true
        Kagebot.processMessage(mockMessage)
        calls shouldBe mutableListOf("access granted")
    }
    "should accept restricted command with permissions" {
        val calls = mutableListOf<String>()
        val mockMessage = mockMessage("!restricted", replies = calls)
        every { mockMessage.messageAuthor.asUser() } returns mockk {
            every { isPresent } returns true
            every { get().getRoles(any()) } returns listOf(
                mockk { every { id } returns 452034011393425409 }
            )
        }
        Kagebot.processMessage(mockMessage)
        calls shouldBe mutableListOf("access granted")
    }
    "should deny command to excluded roles" {
        val calls = mutableListOf<String>()
        val mockMessage = mockMessage("!almostUnrestricted", replies = calls)
        // with the banned role
        every { mockMessage.messageAuthor.asUser() } returns mockk {
            every { isPresent } returns true
            every { get().getRoles(any()) } returns listOf(
                mockk { every { id } returns 452034011393425409 }
            )
        }
        Kagebot.processMessage(mockMessage)

        // without the role
        every { mockMessage.messageAuthor.asUser() } returns mockk {
            every { isPresent } returns true
            every { get().getRoles(any()) } returns emptyList()
        }
        Kagebot.processMessage(mockMessage)
        calls shouldBe mutableListOf(config.localization.permissionDenied, "access granted")
    }
    /*
     * This implicitly tests that the message author is not included in anonymous complaints
     * because getting the author’s name from the mock is undefined.
     */
    "should redirect" {
        val calls = mutableListOf<EmbedBuilder>()
        TestUtil.prepareServerConfig(calls)
        val message = "this is a message"
        Kagebot.processMessage(mockMessage("!anonRedirect $message"))
        calls.size shouldBe 1
        embedToString(calls[0]) shouldContain "\"$message\""
    }
})