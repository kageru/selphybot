package moe.kageru.kagebot.config

import arrow.core.Either
import kotlinx.coroutines.runBlocking
import moe.kageru.kagebot.Globals
import moe.kageru.kagebot.config.SystemSpec.serverId
import java.io.File

object ConfigParser {
  internal const val DEFAULT_CONFIG_PATH = "config.toml"
  val configFile: File = File(DEFAULT_CONFIG_PATH)

  fun initialLoad(file: String) = runBlocking {
    Either.catch {
      val configFile = getFile(file)
      val config = Config.systemSpec.file(configFile)
      Config.system = config
      Config.server = Globals.api.getServerById(config[serverId])
        .orElseThrow { IllegalArgumentException("Invalid server configured.") }
      Config.localization = Config.localeSpec.file(configFile)
      Config.featureConfig = Config.featureSpec.file(configFile)
      Config.commandConfig = Config.commandSpec.file(configFile)
    }
  }

  private fun getFile(path: String): File {
    val file = File(path)
    if (file.isFile) {
      return file
    }
    println("Config not found, falling back to defaults...")
    return File(this::class.java.classLoader.getResource(path)!!.toURI())
  }
}
