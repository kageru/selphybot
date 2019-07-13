package moe.kageru.kagebot

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.ShouldSpec
import moe.kageru.kagebot.config.Config

class ConfigTest : ShouldSpec({
    TestUtil.prepareTestEnvironment()
    "should properly parse test config" {
        Config.systemConfig shouldNotBe null
        Config.localization shouldNotBe null
        Config.features shouldNotBe null
        Config.commands.size shouldBe 2
    }
})
