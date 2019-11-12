package moe.kageru.kagebot.extensions

import arrow.core.ListK
import arrow.core.Option
import arrow.core.k
import org.javacord.api.entity.channel.ServerTextChannel
import moe.kageru.kagebot.Util.asOption
import moe.kageru.kagebot.config.Config
import org.javacord.api.entity.channel.ChannelCategory
import org.javacord.api.entity.permission.Role
import org.javacord.api.entity.server.Server
import org.javacord.api.entity.user.User

fun Server.channelById(id: String): Option<ServerTextChannel> = getTextChannelById(id).asOption()
fun Server.channelByName(name: String): ListK<ServerTextChannel> = getTextChannelsByName(name).k()
fun Server.rolesByName(name: String): ListK<Role> = getRolesByNameIgnoreCase(name).k()
fun Server.membersByName(name: String): ListK<User> = getMembersByName(name).toList().k()
fun Server.memberById(name: Long): Option<User> = getMemberById(name).asOption()
fun Server.categoriesByName(name: String): ListK<ChannelCategory> = getChannelCategoriesByNameIgnoreCase(name).k()

fun User.roles(): ListK<Role> = getRoles(Config.server).k()
