package me.phantomx.pekonime.bot.controller

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.UnprocessedHandler
import eu.vendeli.tgbot.api.message
import eu.vendeli.tgbot.interfaces.sendAsync
import eu.vendeli.tgbot.types.ParseMode
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import eu.vendeli.tgbot.types.internal.Response
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.USER_ADMIN_ID
import me.phantomx.pekonime.bot.extension.loadPrivateChatBotData
import me.phantomx.pekonime.bot.npGson
import me.phantomx.pekonime.bot.privateChatData
import org.slf4j.LoggerFactory

class AdminReplyPrivateChatController {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @UnprocessedHandler
    suspend fun onAdminReply(update: ProcessedUpdate, bot: TelegramBot) {
        if (update.user.id != USER_ADMIN_ID) return

        val msg = update.fullUpdate.message ?: return
        val text = msg.text ?: return

        val replyTo = msg.replyToMessage?.messageId ?: return

        loadPrivateChatBotData()
        val data = privateChatData[replyTo] ?: return

        when(val res = message {
            text
        }.options {
            parseMode = ParseMode.HTML
            replyToMessageId = data.replyMsgId
        }.sendAsync(data.chatId, bot).await()) {
            is Response.Failure -> logger.error("Failure send message to reply - ${npGson.toJson(data)}")
            is Response.Success -> // the data in privateChatData should be deleted to reduce storage
                message {
                    "Reply status ${if (res.ok) "success" else "failure"}"
                }.options {
                    replyToMessageId = msg.messageId
                }.send(update.user, bot)
        }

    }

}