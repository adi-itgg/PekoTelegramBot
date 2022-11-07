package me.phantomx.pekonime.bot.extension

import com.google.gson.reflect.TypeToken
import eu.vendeli.tgbot.api.botactions.setMyCommands
import eu.vendeli.tgbot.interfaces.Action
import eu.vendeli.tgbot.interfaces.Event
import eu.vendeli.tgbot.types.ParseMode
import eu.vendeli.tgbot.types.User
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildResources.DATA_BOT_PC_DATA_JSON
import me.phantomx.pekonime.bot.bot
import me.phantomx.pekonime.bot.data.BotPrivateChatData
import me.phantomx.pekonime.bot.gson
import me.phantomx.pekonime.bot.privateChatData

val User.mention: String get() = "<a href=\"tg://user?id=$id\">@$firstName</a>"

/**
 * if you get message from [BuildConfig] should call this function and contains space with large string
 */
val String.GET: String get() = replace("~nl~", " ")

suspend fun messageHtml(id: Long, message: () -> String) =
    eu.vendeli.tgbot.api.message(message).options { parseMode = ParseMode.HTML }.send(id, bot)
suspend fun messageHtml(user: User, message: () -> String) = messageHtml(user.id, message)
suspend fun messageHtml(user: User, message: String) = messageHtml(user.id) { message }
suspend fun messageHtml(e: Event, message: String) = messageHtml(
    id = e.fullUpdate.message?.chat?.id ?: e.fullUpdate.callbackQuery?.message?.chat?.id ?: e.user.id
) { message }

suspend fun <T> Action<T>.send(e: Event) {
    send(e.fullUpdate.message?.chat?.id ?: e.fullUpdate.callbackQuery?.message?.chat?.id ?: e.user.id, e.bot)
}

suspend fun registerMyCommands() = setMyCommands {
    BuildConfig.COMMANDS.commands.forEach { (k, v) ->
        botCommand("/$k", v)
    }
}.send(bot)


fun loadPrivateChatBotData() {
    if (privateChatData.isNotEmpty()) return
    val type = object : TypeToken< MutableMap<Long, BotPrivateChatData>>(){}.type
    privateChatData = gson.fromJson(DATA_BOT_PC_DATA_JSON.get(), type) ?: return
}