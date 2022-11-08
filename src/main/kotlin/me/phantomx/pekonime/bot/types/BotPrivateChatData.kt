package me.phantomx.pekonime.bot.types

import com.google.gson.reflect.TypeToken
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildResources
import me.phantomx.pekonime.bot.gson
import me.phantomx.pekonime.bot.privateChatData

data class BotPrivateChatData(
    val text: String,
    val userId: Long,
    val chatId: Long,
    val replyMsgId: Long
)

fun loadPrivateChatBotData() {
    if (privateChatData.isNotEmpty()) return
    val type = object : TypeToken<MutableMap<Long, BotPrivateChatData>>(){}.type
    privateChatData = gson.fromJson(BuildResources.DATA_BOT_PC_DATA_JSON.get, type) ?: return
}