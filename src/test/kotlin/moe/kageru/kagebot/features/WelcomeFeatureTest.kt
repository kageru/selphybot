package moe.kageru.kagebot.features

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import moe.kageru.kagebot.TestUtil
import moe.kageru.kagebot.config.Config
import org.javacord.api.entity.message.embed.EmbedBuilder

@ExperimentalStdlibApi
class WelcomeFeatureTest : StringSpec({
  TestUtil.prepareTestEnvironment()
  "should send welcome" {
    val sentMessages = mutableListOf<EmbedBuilder>()
    Config.features.welcome!!.welcomeUser(
      mockk {
        every { user } returns mockk {
          every { discriminatedName } returns "testuser#1234"
          every { sendMessage(capture(sentMessages)) } returns mockk {
            every { join() } returns mockk()
            every { isCompletedExceptionally } returns false
          }
        }
      }
    )
    sentMessages shouldBe mutableListOf(Config.features.welcome!!.embed)
  }
  "should send welcome fallback if DMs are disabled" {
    val message = mutableListOf<String>()
    TestUtil.prepareTestEnvironment(sentMessages = message)
    Config.features.welcome!!.welcomeUser(
      mockk {
        every { user } returns mockk {
          every { discriminatedName } returns "testuser#1234"
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
