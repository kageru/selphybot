package moe.kageru.kagebot

import arrow.core.extensions.list.foldable.find
import moe.kageru.kagebot.Util.checked
import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.config.ConfigParser
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
    Config.commands
      .find { it.matches(readableMessageContent) && it.isAllowed(this) }
      .map { it.execute(this) }
  }

  private fun MessageCreateEvent.handleOwn() {
    if (messageAuthor.isYourself) {
      val loggedMessage = readableMessageContent.ifBlank { "[embed]" }
      Log.info("<Self> $loggedMessage")
    }
  }

  fun init() {
    val secret = File("secret").readText().trim()
    val api = DiscordApiBuilder().setToken(secret).setAllIntents().login().join()
    Globals.api = api
    ConfigParser.initialLoad(ConfigParser.DEFAULT_CONFIG_PATH).mapLeft { e ->
      println("Config parsing error:\n$e,\n${e.message},\n${e.stackTrace.joinToString("\n")}")
      println("Caused by: ${e.cause}\n${e.cause?.stackTrace?.joinToString("\n")}")
      exitProcess(1)
    }
    Runtime.getRuntime().addShutdownHook(
      Thread {
        Log.info("Bot has been interrupted. Shutting down.")
        Dao.setCommandCounter(Globals.commandCounter.get())
        Dao.close()
        api.disconnect()
      },
    )
    Log.info("kagebot Mk II running")
    api.addMessageCreateListener { checked { it.process() } }
    Config.features.eventFeatures().forEach { it.register(api) }
    CronD.startAll()
  }
}
