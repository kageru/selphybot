package moe.kageru.kagebot

import io.kotlintest.shouldNotBe
import io.kotlintest.specs.StringSpec
import moe.kageru.kagebot.config.RawConfig

class ConfigTest : StringSpec({
    /*
    "should properly parse default config" {
        Config.config shouldNotBe null
        Config.config.commands shouldNotBe null
    }
     */
    "should convert to raw config" {
        RawConfig.config shouldNotBe null
    }
})