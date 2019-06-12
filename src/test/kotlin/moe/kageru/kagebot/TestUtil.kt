package moe.kageru.kagebot

import io.kotlintest.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.javacord.api.entity.channel.ServerTextChannel
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.server.Server
import org.javacord.api.entity.user.User
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.core.entity.message.embed.EmbedBuilderDelegateImpl
import java.util.*

object TestUtil {
    fun mockMessage(
        content: String,
        author: Long = 1,
        replies: MutableList<String> = mutableListOf(),
        isSelf: Boolean = false
    ): MessageCreateEvent {
        val message = mockk<MessageCreateEvent>()
        every { message.messageContent } returns content
        every { message.readableMessageContent } returns content
        every { message.messageAuthor.id } returns author
        every { message.channel.sendMessage(capture(replies)) } returns mockk()
        every { message.messageAuthor.isYourself } returns isSelf
        every { message.message.canYouDelete() } returns true
        every { message.messageAuthor.isBotOwner } returns false
        return message
    }

    fun messageableAuthor(messages: MutableList<EmbedBuilder> = mutableListOf()): Optional<User> {
        return mockk {
            every { isPresent } returns true
            every { get() } returns mockk {
                every { getRoles(any()) } returns emptyList()
                every { sendMessage(capture(messages)) } returns mockk()
            }
        }
    }

    fun prepareServerConfig(sentMessages: MutableList<EmbedBuilder> = mutableListOf()) {
        val channelMock = mockk<ServerTextChannel>()
        every { channelMock.sendMessage(capture(sentMessages)) } returns mockk()
        val resultMock = mockk<Optional<ServerTextChannel>>()
        every { resultMock.isPresent } returns true
        every { resultMock.get() } returns channelMock
        val server = mockk<Server>()
        every { server.icon.ifPresent(any()) } just Runs
        every { server.getTextChannelById(any<Long>()) } returns resultMock
        //Config.server = server
    }

    fun testMessageSuccess(content: String, result: String) {
        val calls = mutableListOf<String>()
        Kagebot.processMessage(mockMessage(content, replies = calls))
        calls shouldBe mutableListOf(result)
    }

    fun embedToString(embed: EmbedBuilder): String {
        return (embed.delegate as EmbedBuilderDelegateImpl).toJsonNode().toString()
    }
}