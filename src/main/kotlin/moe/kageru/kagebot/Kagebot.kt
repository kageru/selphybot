package moe.kageru.kagebot

import moe.kageru.kagebot.Log.log
import moe.kageru.kagebot.Util.checked
import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.config.RawConfig
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.event.server.member.ServerMemberJoinEvent
import java.io.File
import kotlin.system.exitProcess

fun main() {
    Kagebot.init()
}

object Kagebot {
    fun processMessage(event: MessageCreateEvent) {
        if (event.messageAuthor.isBotUser) {
            if (event.messageAuthor.isYourself) {
                log.info("<Self> ${event.readableMessageContent}")
            }
            return
        }
        for (command in Globals.commands) {
            if (command.matches(event.messageContent)) {
                command.execute(event)
                break
            }
        }
    }

    fun welcomeUser(event: ServerMemberJoinEvent) {
        Globals.config.features.welcome!!.let { welcome ->
            val message = event.user.sendMessage(welcome.embed)
            // If the user disabled direct messages, try the fallback (if defined)
            if (!Util.wasSuccessful(message) &&
                welcome.fallbackChannel != null &&
                welcome.fallbackMessage != null
            ) {
                welcome.fallbackChannel.sendMessage(
                    welcome.fallbackMessage.replace(
                        "@@",
                        MessageUtil.mention(event.user)
                    )
                )
            }
        }
    }

    private fun getSecret() = File("secret").readText().replace("\n", "")

    fun init() {
        Globals.api = DiscordApiBuilder().setToken(getSecret()).login().join()
        try {
            Globals.config = Config(RawConfig.read())
        } catch (e: IllegalArgumentException) {
            println("Config error:\n$e,\n${e.message},\n${e.stackTrace.joinToString("\n")}")
            exitProcess(1)
        }
        Runtime.getRuntime().addShutdownHook(Thread {
            log.info("Bot has been interrupted. Shutting down.")
            Globals.api.disconnect()
        })
        log.info("kagebot Mk II running")
        Globals.api.addMessageCreateListener { checked { processMessage(it) } }
        Globals.config.features.welcome?.let {
            Globals.api.addServerMemberJoinListener {
                checked { welcomeUser(it) }
            }
        }
    }
}
