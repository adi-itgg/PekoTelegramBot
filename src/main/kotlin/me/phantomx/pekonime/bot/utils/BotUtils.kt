package me.phantomx.pekonime.bot.utils

import eu.vendeli.tgbot.interfaces.Action
import eu.vendeli.tgbot.interfaces.Event
import eu.vendeli.tgbot.interfaces.sendAsync
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.Response
import kotlinx.coroutines.Deferred


val User.mention: String get() = "<a href=\"tg://user?id=$id\">@$firstName</a>"

suspend fun <T> Action<T>.send(e: Event) {
    send(e.chatId, e.bot)
}

suspend inline fun <reified ReturnType> Action<ReturnType>.sendAsync(
    e: Event
): Deferred<Response<out ReturnType>> =
    sendAsync(e.chatId, e.bot)