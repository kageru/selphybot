package moe.kageru.kagebot

import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.javacord.api.entity.message.embed.EmbedBuilder
class FeatureTest
/*
class FeatureTest : StringSpec({
    "should send welcome" {
        val sentMessages = mutableListOf<EmbedBuilder>()
        Kagebot.welcomeUser(
            mockk {
                every { user } returns mockk {
                    every { id } returns 123
                    every { sendMessage(capture(sentMessages)) }
                }
            }
        )
        sentMessages.size shouldBe 1
        TestUtil.embedToString(sentMessages[0]).let { embed ->
            Config.config.features!!.welcome!!.content!!.entries.forEach { (title, content) ->
                embed shouldContain title
                embed shouldContain content
            }
        }
    }
    "should send welcome fallback if DMs are disabled" {
        val dm = slot<String>()
        val sentMessages = mutableListOf<EmbedBuilder>()
        TestUtil.prepareServerConfig(sentMessages)
        Kagebot.welcomeUser(
            mockk {
                every { user } returns mockk {
                    every { id } returns 123
                    every { sendMessage(capture(dm)) }
                }
            }
        )
    }
})
 */