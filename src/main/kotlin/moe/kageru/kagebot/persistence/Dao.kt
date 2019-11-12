package moe.kageru.kagebot.persistence

import arrow.core.k
import org.mapdb.DBMaker
import org.mapdb.Serializer

object Dao {
    private val db = DBMaker.fileDB("kagebot.db").fileMmapEnable().transactionEnable().make()
    private val prisoners = db.hashMap("timeout", Serializer.LONG, Serializer.LONG_ARRAY).createOrOpen()
    private val commands = db.hashMap("commands", Serializer.STRING, Serializer.INTEGER).createOrOpen()
    private val tempVcs = db.hashSet("vcs", Serializer.STRING).createOrOpen()

    fun saveTimeout(releaseTime: Long, user: Long, roles: List<Long>) {
        prisoners[releaseTime] = (listOf(user) + roles).toLongArray()
    }

    fun setCommandCounter(count: Int) {
        commands["total"] = count
    }

    fun getCommandCounter() = commands["total"] ?: 0

    fun close() = db.close()

    fun getAllTimeouts() = prisoners.keys.k()

    fun deleteTimeout(releaseTime: Long): List<Long> {
        val timeout = prisoners[releaseTime]!!
        prisoners.remove(releaseTime)
        return timeout.toList()
    }

    fun isTemporaryVC(channel: String): Boolean {
        return channel in tempVcs
    }

    fun addTemporaryVC(channel: String) {
        tempVcs.add(channel)
    }

    fun removeTemporaryVC(channel: String) {
        tempVcs.remove(channel)
    }
}
