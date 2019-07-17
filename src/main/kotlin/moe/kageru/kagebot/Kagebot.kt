package moe.kageru.kagebot

import moe.kageru.kagebot.Util.checked
import moe.kageru.kagebot.Util.failed
import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.config.ConfigParser
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
    fun MessageCreateEvent.process() {
         if (messageAuthor.isBotUser) {
            if (messageAuthor.isYourself) {
                val loggedMessage =
                    if (readableMessageContent.isBlank()) "[embed]" else readableMessageContent
                Log.info("<Self> $loggedMessage")
            }
            return
        }
        for (command in Config.commands) {
            if (command.matches(readableMessageContent)) {
                command.execute(this)
                break
            }
        }
    }

    fun welcomeUser(event: ServerMemberJoinEvent) {
        Config.features.welcome!!.run {
            val message = event.user.sendMessage(embed)
            // If the user disabled direct messages, try the fallback (if defined)
            if (message.failed() && hasFallback()) {
                fallbackChannel!!.sendMessage(
                    fallbackMessage!!.replace("@@", MessageUtil.mention(event.user))
                )
            }
        }
    }

    private fun getSecret() = File("secret").readText().trim()

    fun init() {
        Globals.api = DiscordApiBuilder().setToken(getSecret()).login().join()
        try {
            ConfigParser.initialLoad(RawConfig.read())
        } catch (e: IllegalArgumentException) {
            println("Config error:\n$e,\n${e.message},\n${e.stackTrace.joinToString("\n")}")
            exitProcess(1)
        }
        Runtime.getRuntime().addShutdownHook(Thread {
            Log.info("Bot has been interrupted. Shutting down.")
            Globals.api.disconnect()
        })
        Log.info("kagebot Mk II running")
        Globals.api.addMessageCreateListener { checked { it.process() } }
        Config.features.welcome?.let {
            Globals.api.addServerMemberJoinListener {
                checked { welcomeUser(it) }
            }
        }
    }
}
