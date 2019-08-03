package moe.kageru.kagebot

import moe.kageru.kagebot.persistence.Dao
import org.javacord.api.DiscordApi
import java.util.concurrent.atomic.AtomicInteger

object Globals {
    lateinit var api: DiscordApi
    val commandCounter: AtomicInteger = AtomicInteger(Dao.getCommandCounter())
}
