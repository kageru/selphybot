package moe.kageru.kagebot.features

import io.kotlintest.shouldBe
import io.kotlintest.specs.ShouldSpec
import moe.kageru.kagebot.Kagebot.process
import moe.kageru.kagebot.TestUtil
import moe.kageru.kagebot.TestUtil.mockMessage
import moe.kageru.kagebot.TestUtil.withCommands
import java.io.File

class ConfigFeatureTest : ShouldSpec({
    TestUtil.prepareTestEnvironment()
    "getConfig should sent message with attachment" {
        withCommands("""
        [[command]]
        trigger = "!getConfig"
        feature = "getConfig"
        """.trimIndent()) {
            val calls = mutableListOf<File>()
            mockMessage("!getConfig", files = calls).process()
            calls.size shouldBe 1
        }
    }
})
