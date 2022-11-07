package me.phantomx.pekonime.bot.utils

import com.google.gson.reflect.TypeToken
import eu.vendeli.tgbot.api.message
import eu.vendeli.tgbot.interfaces.Event
import eu.vendeli.tgbot.interfaces.sendAsync
import eu.vendeli.tgbot.types.ParseMode
import eu.vendeli.tgbot.types.internal.Response
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.USER_ADMIN_ID
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildResources.DATA_BOT_PC_DATA_JSON
import me.phantomx.pekonime.bot.data.BotPrivateChatData
import me.phantomx.pekonime.bot.extension.loadPrivateChatBotData
import me.phantomx.pekonime.bot.extension.mention
import me.phantomx.pekonime.bot.gson
import me.phantomx.pekonime.bot.privateChatData
import java.io.InvalidClassException


inline fun<reified T: Any> String.toClassObj(verify: (T) -> Boolean = { true }): T? {
    val type = object : TypeToken<T>() {}.type
    return try {
        gson.fromJson<T>(this, type).apply {
            if (!verify(this)) throw InvalidClassException("Validate the class failure!")
        }
    } catch (e: Exception) {
        null
    }
}

suspend fun Event.notifyAdminMessage() {
    val msg = fullUpdate.message ?: return
    val text = msg.text ?: return

    when (val res = message {
        "$text\n<pre>by</pre> ${user.mention}"
    }.options {
        parseMode = ParseMode.HTML
    }.sendAsync(USER_ADMIN_ID, bot).await()) {
        is Response.Failure -> {}
        is Response.Success -> {
            loadPrivateChatBotData()
            privateChatData[res.result.messageId] = BotPrivateChatData(
                text = text,
                userId = user.id,
                chatId = msg.chat.id,
                replyMsgId = msg.messageId
            )
            DATA_BOT_PC_DATA_JSON.write(gson.toJson(privateChatData))
        }
    }
}