package moe.kageru.kagebot

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.javacord.api.entity.channel.ServerTextChannel
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.server.Server
import org.javacord.api.event.message.MessageCreateEvent
import java.util.*

object TestUtil {
    fun mockMessage(
        content: String,
        author: Long = 1,
        capturedCalls: MutableList<String> = mutableListOf(),
        isSelf: Boolean = false
    ): MessageCreateEvent {
        val message = mockk<MessageCreateEvent>()
        every { message.messageContent } returns content
        every { message.readableMessageContent } returns content
        every { message.messageAuthor.id } returns author
        every { message.channel.sendMessage(capture(capturedCalls)) } returns mockk()
        every { message.messageAuthor.isYourself } returns isSelf
        every { message.message.canYouDelete() } returns true
        every { message.messageAuthor.isBotOwner } returns false
        return message
    }

    fun prepareServerConfig(sentMessages: MutableList<EmbedBuilder> = mutableListOf()) {
        val channelMock = mockk<ServerTextChannel>()
        every { channelMock.sendMessage(capture(sentMessages)) } returns mockk()
        val resultMock = mockk<Optional<ServerTextChannel>>()
        every { resultMock.isPresent } returns true
        every {resultMock.get()} returns channelMock
        val server = mockk<Server>()
        every { server.icon.ifPresent(any()) } just Runs
        every { server.getTextChannelById(any<Long>()) } returns resultMock
        Config.server = server
    }
}