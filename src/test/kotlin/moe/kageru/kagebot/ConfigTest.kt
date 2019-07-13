package moe.kageru.kagebot

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.ShouldSpec

class ConfigTest : ShouldSpec({
    TestUtil.prepareTestEnvironment()
    "should properly parse test config" {
        Globals.systemConfig shouldNotBe null
        Globals.localization shouldNotBe null
        Globals.features shouldNotBe null
        Globals.commands.size shouldBe 2
    }
})
