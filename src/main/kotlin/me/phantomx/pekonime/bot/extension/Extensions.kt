package me.phantomx.pekonime.bot.extension

import com.google.gson.reflect.TypeToken
import eu.vendeli.tgbot.api.forwardMessage
import eu.vendeli.tgbot.interfaces.Event
import eu.vendeli.tgbot.interfaces.sendAsync
import eu.vendeli.tgbot.types.internal.onSuccess
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.USER_ADMIN_ID
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildResources.DATA_BOT_PC_DATA_JSON
import me.phantomx.pekonime.bot.gson
import me.phantomx.pekonime.bot.privateChatData
import me.phantomx.pekonime.bot.types.BotPrivateChatData
import me.phantomx.pekonime.bot.types.loadPrivateChatBotData
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
    if (msg.text == null && msg.document == null) return
    val text = msg.text ?: msg.caption ?: ""


    forwardMessage(USER_ADMIN_ID, chatId, msg.messageId).sendAsync(USER_ADMIN_ID, bot).await()
        .onSuccess {
            loadPrivateChatBotData()
            privateChatData[it.result.messageId] = BotPrivateChatData(
                text = text,
                userId = user.id,
                chatId = msg.chat.id,
                replyMsgId = msg.messageId
            )
            DATA_BOT_PC_DATA_JSON.write { gson.toJson(privateChatData) }
        }
}