package moe.kageru.kagebot

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.logging.FileHandler
import java.util.logging.Formatter
import java.util.logging.LogRecord
import java.util.logging.Logger

object Log {
    private val log: Logger by lazy {
        Logger.getGlobal().apply {
            addHandler(
                FileHandler("kagebot.log", true).apply {
                    formatter = LogFormatter()
                }
            )
        }
    }

    fun info(message: String) {
        log.info(message)
    }

    fun warn(message: String) {
        log.warning(message)
    }
}

private class LogFormatter : Formatter() {
    private val timeFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())

    override fun format(record: LogRecord): String {
        return "[${record.level}] ${timeFormatter.format(record.instant)}: ${record.message}\n"
    }
}
