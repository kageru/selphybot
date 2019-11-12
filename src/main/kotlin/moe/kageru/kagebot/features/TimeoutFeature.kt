package moe.kageru.kagebot.features

import arrow.core.*
import arrow.core.extensions.list.monad.map
import arrow.core.extensions.listk.functorFilter.filter
import arrow.syntax.collections.destructured
import com.fasterxml.jackson.annotation.JsonProperty
import moe.kageru.kagebot.Log
import moe.kageru.kagebot.MessageUtil.sendEmbed
import moe.kageru.kagebot.Util.findRole
import moe.kageru.kagebot.Util.findUser
import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.config.LocalizationSpec
import moe.kageru.kagebot.extensions.memberById
import moe.kageru.kagebot.extensions.on
import moe.kageru.kagebot.extensions.roles
import moe.kageru.kagebot.extensions.unwrap
import moe.kageru.kagebot.persistence.Dao
import org.javacord.api.entity.permission.Role
import org.javacord.api.entity.user.User
import org.javacord.api.event.message.MessageCreateEvent
import java.time.Duration
import java.time.Instant

class TimeoutFeature(@JsonProperty("role") role: String) : MessageFeature {
    private val timeoutRole: Role = findRole(role).unwrap()

    override fun handle(message: MessageCreateEvent) {
        message.readableMessageContent.split(' ', limit = 4).let { args ->
            Either.cond(
                args.size >= 3,
                { Tuple3(args[1], args[2], args.getOrNull(3)) },
                { "Error: expected “<command> <user> <time> [<reason>]”. If the name contains spaces, please use the user ID instead." }
            ).flatMap {
                Tuple3(
                    findUser(it.a).orNull()
                        ?: return@flatMap "Error: User ${it.a} not found, consider using the user ID".left(),
                    it.b.toLongOrNull() ?: return@flatMap "Error: malformed time “${it.b}”".left(),
                    it.c
                ).right()
            }.on { (user, time, _) ->
                applyTimeout(user, time)
            }.fold(
                { message.channel.sendMessage(it) },
                { (user, time, reason) ->
                    user.sendEmbed {
                        addField("Timeout", Config.localization[LocalizationSpec.timeout].replace("@@", "$time"))
                        reason?.let { addField("Reason", it) }
                    }
                }
            )
        }
    }

    private fun applyTimeout(user: User, time: Long) {
        val oldRoles = user.roles()
            .filter { !it.isManaged }
            .onEach { user.removeRole(it) }
            .map { it.id }
        user.addRole(timeoutRole)
        val releaseTime = Instant.now().plus(Duration.ofMinutes(time)).epochSecond
        Dao.saveTimeout(releaseTime, user.id, oldRoles)
        Log.info("Removed roles ${oldRoles.joinToString()} from user ${user.discriminatedName}")
    }

    fun checkAndRelease(): Unit = Dao.getAllTimeouts()
        .filter { releaseTime -> Instant.now().epochSecond > releaseTime }
        .map { Dao.deleteTimeout(it) }
        .map { it.destructured() }
        .forEach { (userId, roleIds) ->
            Config.server.memberById(userId).fold(
                { Log.warn("Tried to free user $userId, but couldn’t find them on the server anymore") },
                { user ->
                    roleIds.forEach { findRole("$it").map(user::addRole) }
                    user.removeRole(timeoutRole)
                    Log.info("Lifted timeout from user ${user.discriminatedName}. Stored roles ${roleIds.joinToString()}")
                }
            )
        }
}
