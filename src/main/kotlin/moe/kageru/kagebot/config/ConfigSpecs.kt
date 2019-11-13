package moe.kageru.kagebot.config

import com.uchuhimo.konf.ConfigSpec
import moe.kageru.kagebot.command.Command
import moe.kageru.kagebot.config.Config.system
import moe.kageru.kagebot.features.Features
import java.awt.Color

object SystemSpec : ConfigSpec() {
    private val rawColor by optional("#1793d0", name = "color")
    val serverId by required<String>()
    val color by kotlin.lazy { Color.decode(system[rawColor])!! }
}

object LocalizationSpec : ConfigSpec() {
    val redirectedMessage by optional("says")
    val messageDeleted by optional("Your message was deleted.")
    val timeout by optional("You have been timed out for @@ minutes.")
}

object CommandSpec : ConfigSpec(prefix = "") {
    val command by optional(emptyList<Command>())
}

object FeatureSpec : ConfigSpec(prefix = "") {
    val features by optional(Features(), name = "feature")
}
