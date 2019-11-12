package moe.kageru.kagebot.command

import arrow.core.ListK
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import moe.kageru.kagebot.Globals
import moe.kageru.kagebot.Kagebot.process
import moe.kageru.kagebot.TestUtil
import moe.kageru.kagebot.TestUtil.embedToString
import moe.kageru.kagebot.TestUtil.messageableAuthor
import moe.kageru.kagebot.TestUtil.mockMessage
import moe.kageru.kagebot.TestUtil.prepareTestEnvironment
import moe.kageru.kagebot.TestUtil.testMessageSuccess
import moe.kageru.kagebot.TestUtil.withCommands
import moe.kageru.kagebot.Util
import moe.kageru.kagebot.Util.unwrap
import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.extensions.roles
import moe.kageru.kagebot.extensions.rolesByName
import moe.kageru.kagebot.persistence.Dao
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.permission.Role
import org.javacord.api.entity.user.User
import java.util.*

class CommandTest : StringSpec({
    prepareTestEnvironment()
    "should increment command counter" {
        withCommands(
            """
            [[command]]
            trigger = "!ping"
            response = "pong"
            """.trimIndent()
        ) {
            val before = Globals.commandCounter.get()
            testMessageSuccess("!ping", "pong")
            Globals.commandCounter.get() shouldBe (before + 1)
        }
    }
    "should match prefix command" {
        withCommands(
            """
            [[command]]
            trigger = "!ping"
            response = "pong"
            """.trimIndent()
        ) {
            testMessageSuccess("!ping", "pong")
        }
    }
    "should print embed for command" {
        val calls = mutableListOf<EmbedBuilder>()
        prepareTestEnvironment(calls)
        val heading = "heading 1"
        val content = "this is the first paragraph of the embed"
        withCommands(
            """
            [[command]]
            trigger = "!embed"
            embed = [ "$heading", "$content" ]
            """.trimIndent()
        ) {
            TestUtil.withReplyContents(expected = listOf(heading, content)) {
                mockMessage("!embed", replyEmbeds = it).process()
            }
        }
    }
    "should match contains command" {
        withCommands(
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
        withCommands(
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
        withCommands(
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
        withCommands(
            """
            [[command]]
            trigger = "!ping"
            response = "pong"
            """.trimIndent()
        ) {
            val calls = mutableListOf<String>()
            mockMessage("!ping", replies = calls, isBot = true).process()
            calls shouldBe mutableListOf()
        }
    }
    "should delete messages and send copy to author" {
        withCommands(
            """
            [[command]]
            trigger = "delet this"
            [command.action]
            delete = true
            """.trimIndent()
        ) {
            val messageContent = "delet this"
            TestUtil.withReplyContents(expected = listOf(messageContent)) {
                val mockMessage = mockMessage(messageContent)
                every { mockMessage.deleteMessage() } returns mockk()
                every { mockMessage.messageAuthor.asUser() } returns Optional.of(messageableAuthor(it))
                mockMessage.process()
            }
        }
    }
    "should refuse command without permissions" {
        withCommands(
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
            mockMessage.process()
            replies shouldBe mutableListOf()
        }
    }
    "should accept restricted command for owner" {
        withCommands(
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
            mockMessage.process()
            calls shouldBe mutableListOf("access granted")
        }
    }
    "should accept restricted command with permissions" {
        withCommands(
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
                every { roles() } returns ListK.just(
                    Config.server.rolesByName("testrole").first()
                )
            })
            mockMessage.process()
            calls shouldBe mutableListOf("access granted")
        }
    }
    "should deny command to excluded roles" {
        withCommands(
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
                    Config.server.rolesByName("testrole").first()
                )
            }
            mockMessage.process()

            // without the role
            every { mockMessage.messageAuthor.asUser() } returns mockk {
                every { isPresent } returns true
                every { get().getRoles(any()) } returns emptyList()
            }
            mockMessage.process()
            // first message didn’t answer anything
            calls shouldBe mutableListOf("access granted")
        }
    }
    "should refuse DM only message in server channel" {
        withCommands(
            """
            [[command]]
            trigger = "!dm"
            response = "access granted"
            [command.permissions]
            onlyDM = true
            """.trimIndent()
        ) {
            val calls = mutableListOf<String>()
            mockMessage("!dm", replies = calls).process()
            calls shouldBe mutableListOf()
        }
    }
    /*
     * This implicitly tests that the message author is not included in anonymous complaints
     * because getting the author’s name from the mock is undefined.
     */
    "should redirect" {
        val calls = mutableListOf<EmbedBuilder>()
        prepareTestEnvironment(calls)
        withCommands(
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
            mockMessage("!redirect $message").process()
            calls.size shouldBe 1
            embedToString(calls[0]) shouldContain "\"$message\""
        }
    }
    "should assign" {
        withCommands(
            """
            [[command]]
            trigger = "!assign"
            [command.action.assign]
            role = "testrole"
            """.trimIndent()
        ) {
            val roles = mutableListOf<Role>()
            val user = mockk<User> {
                every { addRole(capture(roles), "Requested via command.") } returns mockk()
            }
            every { Config.server.getMemberById(1) } returns Optional.of(user)
            mockMessage("!assign").process()
            roles shouldBe mutableListOf(Util.findRole("testrole").unwrap())
        }
    }
    "should create VC" {
        withCommands(
            """
        [[command]]
        trigger = "!vc"
        feature = "vc"
        """.trimIndent()
        ) {
            testMessageSuccess("!vc 2", "Done")
            Dao.isTemporaryVC("12345") shouldBe true
            Dao.removeTemporaryVC("12345")
        }
    }
    "should reject invalid vc command" {
        withCommands(
            """
        [[command]]
        trigger = "!vc"
        feature = "vc"
        """.trimIndent()
        ) {
            testMessageSuccess("!vc asd", "Invalid syntax, expected a number, got asd")
            Dao.isTemporaryVC("12345") shouldBe false
        }
    }
})
