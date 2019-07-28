package moe.kageru.kagebot.cron

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.kageru.kagebot.config.Config

object CronD {
    fun startAll() {
        GlobalScope.launch {
            minutely()
        }
    }

    private suspend fun minutely() {
        while (true) {
            Config.features.timeout?.checkAndRelease()
            delay(60_000)
        }
    }
}
