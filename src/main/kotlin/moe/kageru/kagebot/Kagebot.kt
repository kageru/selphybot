package moe.kageru.kagebot

import moe.kageru.kagebot.Log.log
import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.config.RawConfig
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.event.server.member.ServerMemberJoinEvent
import java.io.File

class Kagebot {
    companion object {
        fun processMessage(event: MessageCreateEvent) {
            if (event.messageAuthor.isYourself) {
                return
            }
            for (command in Globals.config.commands) {
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
                if (!Util.wasSuccessful(message)
                    && welcome.fallbackChannel != null
                    && welcome.fallbackMessage != null
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
    }

    init {
        Globals.api = DiscordApiBuilder().setToken(getSecret()).login().join()
        try {
            Globals.config = Config(RawConfig.read())
        } catch (e: IllegalArgumentException) {
            println("Config error:\n$e,\n${e.message},\n${e.stackTrace}")
            System.exit(1)
        }
        Runtime.getRuntime().addShutdownHook(Thread {
            log.info("Bot has been interrupted. Shutting down.")
            Globals.api.disconnect()
        })
        log.info("kagebot Mk II running")
        Globals.api.addMessageCreateListener { processMessage(it) }
        Globals.config.features.welcome?.let { welcome ->
            if (welcome.enabled) {
                Globals.api.addServerMemberJoinListener { welcomeUser(it) }
            }
        }
    }

    private fun getSecret() = File("secret").readText().replace("\n", "")
}
