package moe.kageru.kagebot

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.config.LocalizationSpec
import moe.kageru.kagebot.config.SystemSpec
import moe.kageru.kagebot.features.SetConfigFeature
import java.awt.Color

@ExperimentalStdlibApi
class ConfigTest : StringSpec() {
  init {
    "should properly parse test config" {
      TestUtil.prepareTestEnvironment()
      Config.system[SystemSpec.serverId] shouldNotBe null
      SystemSpec.color shouldBe Color.decode("#1793d0")
      Config.features.welcome!!.embed shouldNotBe null
      Config.commands.size shouldBe 3
    }

    "should parse test config via command" {
      val redir = "says"
      val testConfig = """
      [localization]
      redirectedMessage = "$redir"
      messageDeleted = "dongered"
      timeout = "timeout"
      """.trimIndent()
      val message = TestUtil.mockMessage("anything")
      every { message.messageAttachments } returns listOf(
        mockk {
          every { url.openStream().readAllBytes() } returns testConfig.toByteArray()
        },
      )
      SetConfigFeature().handle(message)
      Config.localization[LocalizationSpec.redirectedMessage] shouldBe redir
    }
  }
}
