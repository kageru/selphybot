package moe.kageru.kagebot.features

import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import moe.kageru.kagebot.Globals
import moe.kageru.kagebot.TestUtil
import moe.kageru.kagebot.TestUtil.withReplyContents
import moe.kageru.kagebot.TestUtil.mockMessage
import moe.kageru.kagebot.TestUtil.withCommands
import moe.kageru.kagebot.config.RawHelpFeature
import org.javacord.api.entity.message.embed.EmbedBuilder
import java.util.Optional

class HelpFeatureTest : StringSpec({
    val sentEmbeds = mutableListOf<EmbedBuilder>()
    TestUtil.prepareTestEnvironment(sentEmbeds = sentEmbeds)
    val commandConfig = """
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
                HelpFeature(RawHelpFeature(true))
                    .handle(message = mockMessage("!help", replyEmbeds = replies))
                //Kagebot.processMessage(TestUtil.mockMessage("!help", replyEmbeds = replies))
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
                        Globals.server.getRolesByNameIgnoreCase("testrole")[0]
                    )
                })
                HelpFeature(RawHelpFeature(true))
                    .handle(message = message)
                //Kagebot.processMessage(TestUtil.mockMessage("!help", replyEmbeds = replies))
            }
        }
    }
})

