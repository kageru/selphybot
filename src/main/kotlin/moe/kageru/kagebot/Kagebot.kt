package moe.kageru.kagebot

import moe.kageru.kagebot.Util.checked
import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.config.ConfigParser
import moe.kageru.kagebot.config.RawConfig
import moe.kageru.kagebot.cron.CronD
import moe.kageru.kagebot.persistence.Dao
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.event.message.MessageCreateEvent
import java.io.File
import kotlin.system.exitProcess

fun main() {
    Kagebot.init()
}

object Kagebot {
    fun MessageCreateEvent.process() {
        if (messageAuthor.isBotUser) {
            handleOwn()
            return
        }
        for (command in Config.commands) {
            if (command.matches(readableMessageContent)) {
                // only break if we have the permissions to execute this command, else keep searching
                if (command.execute(this)) {
                    break
                }
            }
        }
    }

    private fun MessageCreateEvent.handleOwn() {
        if (messageAuthor.isYourself) {
            val loggedMessage = readableMessageContent.ifBlank { "[embed]" }
            Log.info("<Self> $loggedMessage")
        }
    }

    private val secret by lazy { File("secret").readText().trim() }

    fun init() {
        val api = DiscordApiBuilder().setToken(secret).login().join()
        Globals.api = api
        try {
            ConfigParser.initialLoad(RawConfig.DEFAULT_CONFIG_PATH)
        } catch (e: IllegalArgumentException) {
            println("Config error:\n$e,\n${e.message},\n${e.stackTrace.joinToString("\n")}")
            exitProcess(1)
        }
        Runtime.getRuntime().addShutdownHook(Thread {
            Log.info("Bot has been interrupted. Shutting down.")
            Dao.setCommandCounter(Globals.commandCounter.get())
            Dao.close()
            api.disconnect()
        })
        Log.info("kagebot Mk II running")
        api.addMessageCreateListener { checked { it.process() } }
        Config.features.eventFeatures().forEach { it.register(api) }
        CronD.startAll()
    }
}
