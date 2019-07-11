package moe.kageru.kagebot.features

import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import moe.kageru.kagebot.Kagebot
import moe.kageru.kagebot.TestUtil
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.event.message.MessageCreateEvent

class DebugFeatureTest : StringSpec({
    TestUtil.prepareTestEnvironment()
    // this will fail if the bot tries to execute more than it should
    // because the mock does not provide the necessary methods
    "should ignore regular users" {
        val message = TestUtil.mockMessage("!debug")
            every { message.messageAuthor.isBotOwner } returns false
        Kagebot.processMessage(message)
        DebugFeature().handle(message)
        verify(exactly = 0) { message.channel.sendMessage(any<EmbedBuilder>()) }
    }
    "should return something" {
        val message = mockk<MessageCreateEvent> {
            every { messageAuthor.isBotOwner } returns true
            every { readableMessageContent } returns "!debug"
            every { channel.sendMessage(any<EmbedBuilder>()) } returns mockk()
        }
        DebugFeature().handle(message)
        verify(exactly = 1) { message.channel.sendMessage(any<EmbedBuilder>()) }
    }
})