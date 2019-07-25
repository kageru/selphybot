package moe.kageru.kagebot.features

import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import moe.kageru.kagebot.Kagebot.process
import moe.kageru.kagebot.TestUtil
import moe.kageru.kagebot.TestUtil.TEST_ROLE
import moe.kageru.kagebot.persistence.Dao

class TimeoutFeatureTest : StringSpec({
    TestUtil.prepareTestEnvironment()
    "should remove and store roles" {
        TestUtil.mockMessage("!timeout kageru 99999999").process()
        Dao.getAllTimeouts().let {
            it.size shouldBe 1
            val user = Dao.deleteTimeout(it.first())
            user shouldBe arrayOf(123, TEST_ROLE.id)
        }
    }
    "should return error for invalid input" {
        val replies = mutableListOf<String>()
        TestUtil.mockMessage("!timeout kageruWithoutATime", replies = replies).process()
        replies.size shouldBe 1
        replies[0] shouldContain "Error"
    }
    "should catch malformed time" {
        val replies = mutableListOf<String>()
        TestUtil.mockMessage("!timeout kageru this is not a time", replies = replies).process()
        replies.size shouldBe 1
        replies[0] shouldContain "Error"
    }
})