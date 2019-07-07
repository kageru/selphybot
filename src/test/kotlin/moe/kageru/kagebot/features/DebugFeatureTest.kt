package moe.kageru.kagebot.features

import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import moe.kageru.kagebot.TestUtil
import moe.kageru.kagebot.config.RawDebugFeature
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.event.message.MessageCreateEvent

class DebugFeatureTest : StringSpec({
    TestUtil.prepareTestEnvironment()
    // this will fail if the bot tries to execute more than it should
    // because the mock does not provide the necessary methods
    "should ignore regular users" {
        val message = mockk<MessageCreateEvent> {
            every { messageAuthor.isBotOwner } returns false
        }
        DebugFeature(RawDebugFeature(true)).handle(message)
        verify(exactly = 0) { message.channel.sendMessage(any<EmbedBuilder>()) }
    }
    "should return something" {
        val message = mockk<MessageCreateEvent> {
            every { messageAuthor.isBotOwner } returns true
            every { readableMessageContent } returns "!debugstats something"
            every { channel.sendMessage(any<EmbedBuilder>()) } returns mockk()
        }
        DebugFeature(RawDebugFeature(true)).handle(message)
        verify(exactly = 1) { message.channel.sendMessage(any<EmbedBuilder>()) }
    }
})