package moe.kageru.kagebot

import moe.kageru.kagebot.Config.Companion.config
import moe.kageru.kagebot.Log.log
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.event.message.MessageCreateEvent

class Kagebot {
    companion object {
        fun processMessage(event: MessageCreateEvent) {
            if (event.messageAuthor.isYourself) {
                return
            }
            for (command in config.commands) {
                if (command.matches(event.messageContent)) {
                    command.execute(event)
                    break
                }
            }
        }
    }

    init {
        val api = DiscordApiBuilder().setToken(Config.secret).login().join()
        Config.server = api.getServerById(config.system.serverId).orElseThrow()
        Runtime.getRuntime().addShutdownHook(Thread {
            log.info("Bot has been interrupted. Shutting down.")
            api.disconnect()
        })
        log.info("kagebot Mk II running")
        api.addMessageCreateListener { processMessage(it) }
    }
}