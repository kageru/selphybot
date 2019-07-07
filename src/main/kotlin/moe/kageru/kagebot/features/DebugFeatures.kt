package moe.kageru.kagebot.features

import com.sun.management.OperatingSystemMXBean
import moe.kageru.kagebot.Globals
import moe.kageru.kagebot.MessageUtil
import moe.kageru.kagebot.config.RawDebugFeatures
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.event.message.MessageCreateEvent
import java.lang.management.ManagementFactory
import java.time.Duration
import java.time.temporal.ChronoUnit

class DebugFeatures(rawDebugFeatures: RawDebugFeatures): MessageFeature() {
    val enabled: Boolean = rawDebugFeatures.enabled

    override fun handleInternal(message: MessageCreateEvent) {
        if (message.messageAuthor.isBotOwner) {
            if (message.readableMessageContent.startsWith("!debugstats")) {
                message.channel.sendMessage(getPerformanceStats())
            }
        }
    }

    private fun getPerformanceStats(): EmbedBuilder {
        val osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean::class.java)
        val runtime = Runtime.getRuntime()
        return MessageUtil.mapToEmbed(
            mapOf(
                Pair(
                    "Bot:",
                    getBotStats()
                ),
                Pair(
                    "Memory:",
                    getMemoryInfo(runtime, osBean)
                ),
                Pair(
                    "CPU:",
                    getCpuInfo(osBean)
                ),
                Pair(
                    "System:",
                    getOsInfo()
                )
            )
        )
    }

    private fun getBotStats() = "kagebot has been running for ${getBotUptime()}.\n" +
            "During this time, ${Globals.commandCounter.get()} commands have been executed."

    private fun getBotUptime(): String {
        val uptime = Duration.of(ManagementFactory.getRuntimeMXBean().uptime, ChronoUnit.MILLIS)
        return String.format(
            "%d days, %d hours, %d minutes, %d seconds",
            uptime.toDaysPart(),
            uptime.toHoursPart(),
            uptime.toMinutesPart(),
            uptime.toSecondsPart()
        )
    }

    private fun getMemoryInfo(runtime: Runtime, osBean: OperatingSystemMXBean): String {
        val mb = 1024 * 1024
        return "Memory usage: ${(runtime.totalMemory() - runtime.freeMemory()) / mb} MB.\n" +
                "Total system memory: ${osBean.committedVirtualMemorySize / mb}/" +
                "${osBean.totalPhysicalMemorySize / mb} MB."
    }

    private fun getCpuInfo(osBean: OperatingSystemMXBean) =
        "The bot is currently using ${String.format("%.4f", osBean.processCpuLoad * 100)}% of the CPU with " +
                "${Thread.activeCount()} active threads.\n" +
                "Total system load is ${String.format("%.2f", osBean.systemCpuLoad * 100)}%."

    private fun getOsInfo() = "Running on ${System.getProperty("os.name")} " +
            "${System.getProperty("os.version")}-${System.getProperty("os.arch")}.\n"
}