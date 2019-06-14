package moe.kageru.kagebot

import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldNotContain
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import moe.kageru.kagebot.Globals.config
import moe.kageru.kagebot.TestUtil.embedToString
import moe.kageru.kagebot.TestUtil.messageableAuthor
import moe.kageru.kagebot.TestUtil.mockMessage
import moe.kageru.kagebot.TestUtil.testMessageSuccess
import moe.kageru.kagebot.TestUtil.withConfig
import org.javacord.api.entity.message.embed.EmbedBuilder
import java.util.*

class CommandTest : StringSpec({
    TestUtil.prepareTestEnvironment()
    "should match prefix command" {
        withConfig(
            """
            [[command]]
            trigger = "!ping"
            response = "pong"
            """.trimIndent()
        ) {
            testMessageSuccess("!ping", "pong")
        }
    }
    "should match contains command" {
        withConfig(
            """
            [[command]]
            trigger = "somewhere"
            response = "found it"
            matchType = "CONTAINS"
            """.trimIndent()
        ) {
            testMessageSuccess("the trigger is somewhere in this message", "found it")
        }
    }
    "should match regex command" {
        withConfig(
            """
            [[command]]
            trigger = "A.+B"
            response = "regex matched"
            matchType = "REGEX"
            """.trimIndent()
        ) {
            testMessageSuccess("AcsdB", "regex matched")
        }
    }
    "should ping author" {
        withConfig(
            """
            [[command]]
            trigger = "answer me"
            response = "@@ there you go"
            """.trimIndent()
        ) {
            testMessageSuccess("answer me", "<@1> there you go")
        }
    }
    "should not react to own message" {
        withConfig(
            """
            [[command]]
            trigger = "!ping"
            response = "pong"
            """.trimIndent()
        ) {
            val calls = mutableListOf<String>()
            Kagebot.processMessage(mockMessage("!ping", replies = calls, isBot = true))
            calls shouldBe mutableListOf()
        }
    }
    "should delete messages and send copy to author" {
        withConfig(
            """
            [[command]]
            trigger = "delet this"
            [command.action]
            delete = true
            """.trimIndent()
        ) {
            val replies = mutableListOf<EmbedBuilder>()
            val messageContent = "delet this"
            val mockMessage = mockMessage(messageContent)
            every { mockMessage.deleteMessage() } returns mockk()
            every { mockMessage.messageAuthor.asUser() } returns Optional.of(messageableAuthor(replies))
            Kagebot.processMessage(mockMessage)
            verify(exactly = 1) { mockMessage.deleteMessage() }
            replies.size shouldBe 1
            embedToString(replies[0]) shouldContain messageContent
        }
    }
    "should refuse command without permissions" {
        withConfig(
            """
            [[command]]
            trigger = "!restricted"
            response = "access granted"
            [command.permissions]
            hasOneOf = [
                "testrole",
            ]
            """.trimIndent()
        ) {
            val replies = mutableListOf<String>()
            val mockMessage = mockMessage("!restricted", replies = replies)
            every { mockMessage.messageAuthor.asUser() } returns Optional.of(messageableAuthor())
            Kagebot.processMessage(mockMessage)
            replies shouldBe mutableListOf(config.localization.permissionDenied)
        }
    }
    "should accept restricted command for owner" {
        withConfig(
            """
            [[command]]
            trigger = "!restricted"
            response = "access granted"
            [command.permissions]
            hasOneOf = [
                "testrole"
            ]
            """.trimIndent()
        ) {
            val calls = mutableListOf<String>()
            val mockMessage = mockMessage("!restricted", replies = calls)
            every { mockMessage.messageAuthor.isBotOwner } returns true
            Kagebot.processMessage(mockMessage)
            calls shouldBe mutableListOf("access granted")
        }
    }
    "should accept restricted command with permissions" {
        withConfig(
            """
            [[command]]
            trigger = "!restricted"
            response = "access granted"
            [command.permissions]
            hasOneOf = [
                "testrole"
            ]
            """.trimIndent()
        ) {
            val calls = mutableListOf<String>()
            val mockMessage = mockMessage("!restricted", replies = calls)
            every { mockMessage.messageAuthor.asUser() } returns Optional.of(mockk {
                every { getRoles(any()) } returns listOf(
                    Globals.server.getRolesByNameIgnoreCase("testrole")[0]
                )
            })
            Kagebot.processMessage(mockMessage)
            calls shouldBe mutableListOf("access granted")
        }
    }
    "should deny command to excluded roles" {
        withConfig(
            """
            [[command]]
            trigger = "!almostUnrestricted"
            response = "access granted"
            [command.permissions]
            hasNoneOf = ["testrole"]
            """.trimIndent()
        ) {
            val calls = mutableListOf<String>()
            val mockMessage = mockMessage("!almostUnrestricted", replies = calls)
            // with the banned role
            every { mockMessage.messageAuthor.asUser() } returns mockk {
                every { isPresent } returns true
                every { get().getRoles(any()) } returns listOf(
                    Globals.server.getRolesByNameIgnoreCase("testrole")[0]
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
    }
    "should refuse DM only message in server channel" {
        withConfig(
            """
            [[command]]
            trigger = "!dm"
            response = "access granted"
            [command.permissions]
            onlyDM = true
            """.trimIndent()
        ) {
            val calls = mutableListOf<String>()
            Kagebot.processMessage(mockMessage("!dm", replies = calls))
            calls shouldBe listOf(config.localization.permissionDenied)
        }
    }
    /*
     * This implicitly tests that the message author is not included in anonymous complaints
     * because getting the author’s name from the mock is undefined.
     */
    "should redirect" {
        val calls = mutableListOf<EmbedBuilder>()
        TestUtil.prepareTestEnvironment(calls)
        withConfig(
            """
            [[command]]
            trigger = "!redirect"
            response = "redirected"
            [command.action.redirect]
            target = "testchannel"
            anonymous = true
            """.trimIndent()
        ) {
            val message = "this is a message"
            Kagebot.processMessage(mockMessage("!redirect $message", replyEmbeds = calls))
            calls.size shouldBe 1
            embedToString(calls[0]) shouldContain "\"$message\""
        }
    }
})