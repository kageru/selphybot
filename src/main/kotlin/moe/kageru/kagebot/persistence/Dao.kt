package moe.kageru.kagebot.persistence

import org.mapdb.DBMaker
import org.mapdb.Serializer

object Dao {
    private val db = DBMaker.fileDB("kagebot.db").fileMmapEnable().closeOnJvmShutdown().make()
    private val prisoners = db.hashMap("timeout", Serializer.LONG, Serializer.LONG_ARRAY).createOrOpen()

    fun saveTimeout(releaseTime: Long, roles: List<Long>) {
        prisoners[releaseTime] = roles.toLongArray()
    }

    fun getAllTimeouts() = prisoners.keys

    fun deleteTimeout(releaseTime: Long): LongArray {
        val timeout = prisoners[releaseTime]!!
        prisoners.remove(releaseTime)
        return timeout
    }
}