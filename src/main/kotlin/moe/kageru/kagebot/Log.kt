package moe.kageru.kagebot

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.logging.*

object Log {
    val log: Logger by lazy {
        val log = Logger.getGlobal()
        val fh = FileHandler("kagebot.log", true)
        val formatter = LogFormatter()
        fh.formatter = formatter
        log.addHandler(fh)
        return@lazy log
    }
}

private class LogFormatter : Formatter() {
    private val timeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())

    override fun format(record: LogRecord): String {
        return "[${record.level}] ${timeFormatter.format(record.instant)}: ${record.message}\n"
    }
}