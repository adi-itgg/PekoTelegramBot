package me.phantomx.pekonime.bot.extension

import eu.vendeli.tgbot.api.botactions.setMyCommands
import eu.vendeli.tgbot.types.ParseMode
import eu.vendeli.tgbot.types.User
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig
import me.phantomx.pekonime.bot.bot

val User.mention: String get() = "<a href=\"tg://user?id=$id\">@$firstName</a>"

/**
 * if you get message from [BuildConfig] should call this function and contains space with large string
 */
val String.GET: String get() = replace("~nl~", " ")

suspend fun messageHtml(id: Long, message: () -> String) =
    eu.vendeli.tgbot.api.message(message).options { parseMode = ParseMode.HTML }.send(id, bot)
suspend fun messageHtml(user: User, message: () -> String) = messageHtml(user.id, message)
suspend fun messageHtml(user: User, message: String) = messageHtml(user.id) { message }

suspend fun registerMyCommands() = setMyCommands {
    BuildConfig.COMMANDS.commands.forEach { (k, v) ->
        botCommand("/$k", v)
    }
}.send(bot)