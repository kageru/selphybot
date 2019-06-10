package moe.kageru.kagebot

import io.kotlintest.shouldNotBe
import io.kotlintest.specs.StringSpec

class ConfigTest : StringSpec({
    "should properly parse default config" {
        Config.config shouldNotBe null
        Config.config.commands shouldNotBe null
    }
})