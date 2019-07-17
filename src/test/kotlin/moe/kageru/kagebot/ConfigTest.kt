package moe.kageru.kagebot

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.ShouldSpec
import io.mockk.every
import io.mockk.mockk
import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.features.SetConfigFeature

class ConfigTest : ShouldSpec({
    TestUtil.prepareTestEnvironment()
    "should properly parse test config" {
        Config.systemConfig shouldNotBe null
        Config.localization shouldNotBe null
        Config.features shouldNotBe null
        Config.commands.size shouldBe 2
    }

    "should parse test config via command" {
        val denied = "denied"
        val testConfig = """
        [localization]
        permissionDenied = "$denied"
        redirectedMessage = "says"
        messageDeleted = "dongered"
        
        [[command]]
        response = "this command is broken"
        """.trimIndent()
        val message = TestUtil.mockMessage("anything")
        every { message.messageAttachments } returns listOf(mockk {
            every { url.openStream().readAllBytes() } returns testConfig.toByteArray()
        })
        SetConfigFeature().handle(message)
        Config.localization.permissionDenied shouldBe denied
    }
})
