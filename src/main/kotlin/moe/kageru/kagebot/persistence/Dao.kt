package moe.kageru.kagebot.persistence

import org.mapdb.DBMaker
import org.mapdb.Serializer

object Dao {
    private val db = DBMaker.fileDB("kagebot.db").fileMmapEnable().closeOnJvmShutdown().make()
    private val strings = db.hashMap("main", Serializer.STRING, Serializer.STRING).createOrOpen()

    fun store(key: String, value: String) {
        strings[key] = value
    }

    fun get(key: String): String? {
        return strings[key]
    }
}