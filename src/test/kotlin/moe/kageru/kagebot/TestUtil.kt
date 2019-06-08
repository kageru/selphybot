package moe.kageru.kagebot

import io.mockk.every
import io.mockk.mockk
import org.javacord.api.event.message.MessageCreateEvent

object TestUtil {
    fun mockMessage(content: String, author: Long = 1, capturedCalls: MutableList<String> = mutableListOf()): MessageCreateEvent {
        val message = mockk<MessageCreateEvent>()
        every { message.messageContent } returns content
        every { message.messageAuthor.id } returns author
        every { message.channel.sendMessage(capture(capturedCalls)) } returns mockk()
        return message
    }
}