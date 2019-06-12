package moe.kageru.kagebot

import moe.kageru.kagebot.config.Config
import org.javacord.api.DiscordApi
import org.javacord.api.entity.server.Server

object Globals {
    lateinit var server: Server
    lateinit var api: DiscordApi
    lateinit var config: Config
}