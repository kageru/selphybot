package moe.kageru.kagebot

import arrow.core.ListK
import arrow.core.Option
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldNotContain
import io.kotlintest.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import moe.kageru.kagebot.Kagebot.process
import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.config.ConfigParser
import moe.kageru.kagebot.extensions.*
import org.javacord.api.entity.channel.ServerTextChannel
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.permission.Role
import org.javacord.api.entity.user.User
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.core.entity.message.embed.EmbedBuilderDelegateImpl
import java.io.File
import java.util.*

object TestUtil {
    private val TIMEOUT_ROLE = mockk<Role> {
        every { id } returns 123
    }
    val TEST_ROLE = mockk<Role> {
        every { id } returns 1
        every { isManaged } returns false
    }

    fun mockMessage(
        content: String,
        replies: MutableList<String> = mutableListOf(),
        replyEmbeds: MutableList<EmbedBuilder> = mutableListOf(),
        files: MutableList<File> = mutableListOf(),
        isBot: Boolean = false
    ): MessageCreateEvent {
        return mockk {
            every { messageContent } returns content
            every { readableMessageContent } returns content
            every { channel.sendMessage(capture(replies)) } returns mockk(relaxed = true) {
                every { isCompletedExceptionally } returns false
            }
            every { channel.sendMessage(capture(replyEmbeds)) } returns mockk(relaxed = true) {
                every { isCompletedExceptionally } returns false
            }
            every { channel.sendMessage(capture(files)) } returns mockk(relaxed = true) {
                every { isCompletedExceptionally } returns false
            }
            every { message.canYouDelete() } returns true
            every { isPrivateMessage } returns false
            // We can’t use a nested mock here because other fields of messageAuthor might
            // get overwritten by other tests, which would delete a nested mock.
            every { messageAuthor.id } returns 1
            every { messageAuthor.discriminatedName } returns "testuser#1234"
            every { messageAuthor.isBotUser } returns isBot
            every { messageAuthor.isYourself } returns isBot
            every { messageAuthor.isBotOwner } returns false
            every { messageAuthor.asUser() } returns Optional.of(messageableAuthor(replyEmbeds))
            every { messageAuthor.name } returns "kageru"
        }
    }

    fun messageableAuthor(messages: MutableList<EmbedBuilder> = mutableListOf()): User {
        return mockk {
            every { roles() } returns ListK.empty()
            every { sendMessage(capture(messages)) } returns mockk(relaxed = true)
        }
    }

    fun prepareTestEnvironment(
        sentEmbeds: MutableList<EmbedBuilder> = mutableListOf(),
        sentMessages: MutableList<String> = mutableListOf(),
        dmEmbeds: MutableList<EmbedBuilder> = mutableListOf()
    ) {
        val channel = mockk<ServerTextChannel>(relaxed = true) {
            every { sendMessage(capture(sentEmbeds)) } returns mockk(relaxed = true) {
                every { join() } returns mockk {
                    every { isCompletedExceptionally } returns false
                }
                every { isCompletedExceptionally } returns false
            }
            every { sendMessage(capture(sentMessages)) } returns mockk(relaxed = true)
        }
        Globals.api = mockk(relaxed = true) {
            every { getServerById(any<String>()) } returns Optional.of(mockk(relaxed = true) {
                every { icon.ifPresent(any()) } just Runs
                every { channelById(any()) } returns Option.just(channel)
                every { channelsByName(any()) } returns ListK.just(channel)
                every { rolesByName("testrole") } returns ListK.just(TEST_ROLE)
                every { rolesByName("timeout") } returns ListK.just(TIMEOUT_ROLE)
                every { categoriesByName(any()) } returns ListK.just(mockk())
                every { createVoiceChannelBuilder().create() } returns mockk {
                    every { isCompletedExceptionally } returns false
                    every { join().idAsString } returns "12345"
                }
                every { membersByName(any()) } returns ListK.just(mockk(relaxed = true) {
                    every { id } returns 123
                    every { roles() } returns ListK.just(TEST_ROLE)
                    every { sendMessage(capture(dmEmbeds)) } returns mockk(relaxed = true) {
                        every { isCompletedExceptionally } returns false
                    }
                })
            })
        }
        Config.server = Globals.api.getServerById("").get()
        ConfigParser.initialLoad("testconfig.toml")
    }

    fun testMessageSuccess(content: String, result: String) {
        val calls = mutableListOf<String>()
        mockMessage(content, replies = calls).process()
        calls shouldBe mutableListOf(result)
    }

    fun embedToString(embed: EmbedBuilder): String {
        return (embed.delegate as EmbedBuilderDelegateImpl).toJsonNode().toString()
    }

    fun withCommands(config: String, test: (() -> Unit)) {
        val oldCmds = Config.commandConfig
        Config.commandConfig = Config.commandSpec.string(config)
        test()
        Config.commandConfig = oldCmds
    }

    fun withReplyContents(
        expected: List<String> = emptyList(),
        unexpected: List<String> = emptyList(),
        op: (MutableList<EmbedBuilder>) -> Unit
    ) {
        val replies = mutableListOf<EmbedBuilder>()
        op(replies)
        replies.size shouldBe 1
        val replyString = embedToString(replies[0])
        for (string in expected) {
            replyString shouldContain string
        }
        for (string in unexpected) {
            replyString shouldNotContain string
        }
    }
}
