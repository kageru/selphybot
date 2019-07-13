package moe.kageru.kagebot

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.StringSpec

class ConfigTest : StringSpec({
    TestUtil.prepareTestEnvironment()
    "should properly parse test config" {
        Globals.config shouldNotBe null
        Globals.systemConfig shouldNotBe null
        Globals.commands.size shouldBe 2
    }
})
