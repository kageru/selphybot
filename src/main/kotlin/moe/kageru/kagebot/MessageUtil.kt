package moe.kageru.kagebot

import org.javacord.api.entity.message.MessageAuthor

object MessageUtil {
    fun mention(user: MessageAuthor): String {
        return "<@${user.id}>"
    }
}