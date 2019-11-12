package moe.kageru.kagebot.features

import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import moe.kageru.kagebot.Kagebot.process
import moe.kageru.kagebot.TestUtil
import moe.kageru.kagebot.TestUtil.mockMessage
import moe.kageru.kagebot.TestUtil.withCommands
import moe.kageru.kagebot.TestUtil.withReplyContents
import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.extensions.rolesByName
import org.javacord.api.entity.message.embed.EmbedBuilder
import java.util.*

class HelpFeatureTest : StringSpec({
    val sentEmbeds = mutableListOf<EmbedBuilder>()
    TestUtil.prepareTestEnvironment(sentEmbeds = sentEmbeds)
    val commandConfig = """
        [[command]]
        trigger = "!help"
        feature = "help"
        [[command]]
        trigger = "!ping"
        [[command]]
        trigger = "!something"
        [[command]]
        trigger = "not a prefix"
        matchType = "CONTAINS"
        [[command]]
        trigger = "!prison"
        [command.permissions]
        hasOneOf = ["testrole"]
        """.trimIndent()
    "should show prefix command" {
        withCommands(commandConfig) {
            val expected = listOf("!ping", "!something")
            val unexpected = listOf("not a prefix", "!prison")
            withReplyContents(expected = expected, unexpected = unexpected) { replies ->
                mockMessage("!help", replyEmbeds = replies).process()
            }
        }
    }
    "should show moderation commands for mod" {
        withCommands(commandConfig) {
            val expected = listOf("!ping", "!something", "!prison")
            val unexpected = listOf("not a prefix")
            withReplyContents(expected = expected, unexpected = unexpected) { replies ->
                val message = mockMessage("!help", replyEmbeds = replies)
                every { message.messageAuthor.asUser() } returns Optional.of(mockk {
                    every { getRoles(any()) } returns listOf(
                        Config.server.rolesByName("testrole").first()
                    )
                })
                message.process()
            }
        }
    }
})
