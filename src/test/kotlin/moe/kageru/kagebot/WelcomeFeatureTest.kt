package moe.kageru.kagebot

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import org.javacord.api.entity.message.embed.EmbedBuilder

class WelcomeFeatureTest : StringSpec({
    TestUtil.prepareTestEnvironment()
    "should send welcome" {
        val sentMessages = mutableListOf<EmbedBuilder>()
        Kagebot.welcomeUser(
            mockk {
                every { user } returns mockk {
                    every { sendMessage(capture(sentMessages)) } returns mockk {
                        every { join() } returns mockk()
                        every { isCompletedExceptionally } returns false
                    }
                }
            }
        )
        sentMessages shouldBe mutableListOf(Globals.config.features.welcome!!.embed)
    }
    "should send welcome fallback if DMs are disabled" {
        val message = mutableListOf<String>()
        TestUtil.prepareTestEnvironment(sentMessages = message)
        Kagebot.welcomeUser(
            mockk {
                every { user } returns mockk {
                    every { id } returns 123
                    every { sendMessage(any<EmbedBuilder>()) } returns mockk {
                        every { join() } returns mockk()
                        every { isCompletedExceptionally } returns true
                    }
                }
            }
        )
        message shouldBe mutableListOf("<@123> welcome")
    }
})
