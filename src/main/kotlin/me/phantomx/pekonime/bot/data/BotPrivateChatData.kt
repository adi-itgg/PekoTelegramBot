package me.phantomx.pekonime.bot.data

data class BotPrivateChatData(
    val text: String,
    val userId: Long,
    val chatId: Long,
    val replyMsgId: Long
)
