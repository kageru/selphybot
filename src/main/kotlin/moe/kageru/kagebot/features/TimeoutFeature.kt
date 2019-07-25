package moe.kageru.kagebot.features

import moe.kageru.kagebot.Log
import moe.kageru.kagebot.MessageUtil.sendEmbed
import moe.kageru.kagebot.Util.findRole
import moe.kageru.kagebot.Util.findUser
import moe.kageru.kagebot.Util.ifNotEmpty
import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.config.RawTimeoutFeature
import moe.kageru.kagebot.persistence.Dao
import org.javacord.api.entity.permission.Role
import org.javacord.api.event.message.MessageCreateEvent
import java.time.Duration
import java.time.Instant

class TimeoutFeature(raw: RawTimeoutFeature) : MessageFeature {
    private val timeoutRole: Role = raw.role?.let(::findRole)
        ?: throw IllegalArgumentException("No timeout role defined")

    override fun handle(message: MessageCreateEvent) {
        val (target, time) = message.readableMessageContent.split(' ', limit = 3).let {
            if (it.size != 3) {
                message.channel.sendMessage("Error: expected “<command> <user> <time>”. If the name contains spaces, please use the user ID instead.")
                return
            }
            val time = it[2].toLongOrNull()
            if (time == null) {
                message.channel.sendMessage("Error: malformed time")
                return
            }
            Pair(it[1], time)
        }
        findUser(target)?.let { user ->
            val oldRoles = user.getRoles(Config.server)
                .filter { !it.isManaged }
                .map { role ->
                    user.removeRole(role)
                    role.id
                }
            user.addRole(timeoutRole)
            val releaseTime = Instant.now().plus(Duration.ofMinutes(time)).epochSecond
            Dao.saveTimeout(releaseTime, listOf(user.id) + oldRoles)
            user.sendEmbed {
                addField("Timeout", Config.localization.timeout.replace("@@", time.toString()))
            }
            Log.info("Removed roles ${oldRoles.joinToString()} from user ${user.discriminatedName}")
        } ?: message.channel.sendMessage("Could not find user $target. Consider using the user ID.")
    }

    fun checkAndRelease() {
        val now = Instant.now().epochSecond
        Dao.getAllTimeouts()
            .filter { releaseTime -> now > releaseTime }
            .map {
                Dao.deleteTimeout(it).let { rawIds ->
                    UserInTimeout.ofLongs(rawIds).toPair()
                }
            }.forEach { (userId, roleIds) ->
                Config.server.getMemberById(userId).ifNotEmpty { user ->
                    roleIds.forEach { roleId ->
                        user.addRole(findRole("$roleId"))
                    }
                    user.removeRole(timeoutRole)
                    Log.info("Lifted timeout from user ${user.discriminatedName}. Stored roles ${roleIds.joinToString()}")
                } ?: Log.warn("Tried to free user $userId, but couldn’t find them on the server anymore")
            }
    }
}

class UserInTimeout(private val id: Long, private val roles: List<Long>) {
    fun toPair() = Pair(id, roles)

    companion object {
        fun ofLongs(longs: LongArray): UserInTimeout = longs.run {
            val userId = first()
            val roles = if (size > 1) slice(1 until size) else emptyList()
            return UserInTimeout(userId, roles)
        }
    }
}