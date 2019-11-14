package moe.kageru.kagebot

import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.config.SystemSpec
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.MessageAuthor
import org.javacord.api.entity.message.Messageable
import org.javacord.api.entity.message.embed.EmbedBuilder
import java.util.concurrent.CompletableFuture

object MessageUtil {
  fun MessageAuthor.mention() = "<@$id>"

  fun withEmbed(op: EmbedBuilder.() -> Unit): EmbedBuilder {
    return EmbedBuilder().apply {
      Config.server.icon.ifPresent { setThumbnail(it) }
      setColor(SystemSpec.color)
      op()
    }
  }

  fun Messageable.sendEmbed(op: EmbedBuilder.() -> Unit) {
    val embed = withEmbed {
      setTimestampToNow()
      op()
    }
    sendMessage(embed)
  }

  /**
   * Send and embed and add the current time to it.
   * The time is not set in [withEmbed] because of https://git.kageru.moe/kageru/discord-kagebot/issues/13.
   */
  fun sendEmbed(target: Messageable, embed: EmbedBuilder): CompletableFuture<Message> {
    return target.sendMessage(embed.setTimestampToNow())
  }

  /**
   * The reason we use a list here (rather than a map) is that maps would not retain the order specified in the config.
   * I tried LinkedHashMaps, but those don’t seem to work either.
   */
  fun listToEmbed(contents: List<String>): EmbedBuilder {
    check(contents.size % 2 != 1) { "Embed must have even number of content strings (title/content pairs)" }
    return withEmbed {
      contents.toPairs().forEach { (heading, content) ->
        addField(heading, content)
      }
    }
  }

  /**
   * Convert a list of elements to pairs, retaining order.
   * The last element is dropped if the input size is odd.
   * [1, 2, 3, 4, 5] -> [[1, 2], [3, 4]]
   */
  private fun <T> Collection<T>.toPairs(): List<Pair<T, T>> = this.iterator().run {
    (0 until size / 2).map {
      Pair(next(), next())
    }
  }
}
