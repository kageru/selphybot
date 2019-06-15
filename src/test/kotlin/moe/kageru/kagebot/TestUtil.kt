package moe.kageru.kagebot

import io.kotlintest.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.config.RawConfig
import org.javacord.api.DiscordApi
import org.javacord.api.entity.channel.ServerTextChannel
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.user.User
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.core.entity.message.embed.EmbedBuilderDelegateImpl
import java.util.*

object TestUtil {
    fun mockMessage(
        content: String,
        author: Long = 1,
        replies: MutableList<String> = mutableListOf(),
        replyEmbeds: MutableList<EmbedBuilder> = mutableListOf(),
        isBot: Boolean = false
    ): MessageCreateEvent {
        return mockk {
            every { messageContent } returns content
            every { readableMessageContent } returns content
            every { messageAuthor.id } returns author
            every { channel.sendMessage(capture(replies)) } returns mockk()
            every { channel.sendMessage(capture(replyEmbeds)) } returns mockk()
            every { messageAuthor.isBotUser } returns isBot
            every { message.canYouDelete() } returns true
            every { messageAuthor.isBotOwner } returns false
            every { isPrivateMessage } returns false
        }
    }

    fun messageableAuthor(messages: MutableList<EmbedBuilder> = mutableListOf()): User {
        return mockk {
            every { getRoles(any()) } returns emptyList()
            every { sendMessage(capture(messages)) } returns mockk()
        }
    }

    fun prepareTestEnvironment(sentMessages: MutableList<EmbedBuilder> = mutableListOf()) {
        val channel = mockk<Optional<ServerTextChannel>> {
            every { isPresent } returns true
            every { get() } returns mockk {
                every { sendMessage(capture(sentMessages)) } returns mockk {
                    every { join() } returns mockk()
                    every { isCompletedExceptionally } returns false
                }
            }
        }
        val api = mockk<DiscordApi> {
            every { getServerById(any<String>()) } returns Optional.of(mockk {
                every { icon.ifPresent(any()) } just Runs
                every { getTextChannelById(any<String>()) } returns channel
                every { getTextChannelsByName(any()) } returns listOf(channel.get())
                every { getRolesByNameIgnoreCase("testrole") } returns listOf(mockk {
                    every { id } returns 1
                })
            })
        }
        Globals.api = api
        Globals.config = Config(RawConfig.read("testconfig.toml"))
    }

    fun testMessageSuccess(content: String, result: String) {
        val calls = mutableListOf<String>()
        Kagebot.processMessage(mockMessage(content, replies = calls))
        calls shouldBe mutableListOf(result)
    }

    fun embedToString(embed: EmbedBuilder): String {
        return (embed.delegate as EmbedBuilderDelegateImpl).toJsonNode().toString()
    }

    fun <R> withCommands(config: String, test: (() -> R)) {
        val oldCmds = Globals.config.commands
        val rawConfig = RawConfig.readFromString(config)
        Globals.config.reloadCommands(rawConfig)
        test()
        Globals.config.commands = oldCmds
    }

    fun <R> withLocalization(config: String, test: (() -> R)) {
        val oldLoc = Globals.config.localization
        val rawConfig = RawConfig.readFromString(config)
        Globals.config.reloadLocalization(rawConfig.localization!!)
        test()
        Globals.config.localization = oldLoc
    }
}