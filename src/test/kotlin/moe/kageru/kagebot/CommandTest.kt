package moe.kageru.kagebot

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import moe.kageru.kagebot.Config.Companion.config
import org.javacord.api.entity.permission.Role
import org.javacord.api.entity.user.User
import java.util.*

class CommandTest : StringSpec({
    Config.server = mockk()
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
        val mockOptional = mockk<Optional<User>>()
        every { mockOptional.isEmpty } returns false
        every { mockOptional.get().getRoles(any()) } returns emptyList()
        val mockMessage = TestUtil.mockMessage("!restricted", capturedCalls = calls)
        every { mockMessage.messageAuthor.asUser() } returns mockOptional
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
        val mockRole = mockk<Role>()
        every { mockRole.id } returns 452034011393425409
        val mockOptional = mockk<Optional<User>>()
        every { mockOptional.isEmpty } returns false
        every { mockOptional.get().getRoles(any()) } returns listOf(mockRole)
        val mockMessage = TestUtil.mockMessage("!restricted", capturedCalls = calls)
        every { mockMessage.messageAuthor.asUser() } returns mockOptional
        Kagebot.processMessage(mockMessage)
        calls.size shouldBe 1
        calls[0] shouldBe "access granted"
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