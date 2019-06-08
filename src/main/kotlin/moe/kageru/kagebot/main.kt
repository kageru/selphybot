package moe.kageru.kagebot

import moe.kageru.kagebot.Config.Companion.config
import moe.kageru.kagebot.Log.log
import org.javacord.api.DiscordApiBuilder
import java.lang.System

fun main() {
    try {
        createBot()
    } catch (e: Exception) {
        log.warning("An exception occurred in the main thread, exiting. ${e.stackTrace.joinToString("\n")}")
        System.exit(1)
    }
}

fun createBot() {
    val api = DiscordApiBuilder().setToken(Config.secret).login().join()
    println(config.system.admins)
    Runtime.getRuntime().addShutdownHook(Thread {
        log.info("Bot has been interrupted. Shutting down.")
        api.disconnect()
    })
    log.info("kagebot Mk II running")
    api.addMessageCreateListener { event ->
        if (config.commands.commands[0].matches(event.messageContent)) {
            event.channel.sendMessage(config.commands.commands[0].respond())
            println("message was created")
        }
    }

}
