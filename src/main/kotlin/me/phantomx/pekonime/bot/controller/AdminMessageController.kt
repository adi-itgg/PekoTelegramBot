package me.phantomx.pekonime.bot.controller

import eu.vendeli.tgbot.annotations.UnprocessedHandler
import eu.vendeli.tgbot.api.message
import eu.vendeli.tgbot.enums.MethodPriority
import eu.vendeli.tgbot.interfaces.Event
import eu.vendeli.tgbot.interfaces.sendAsync
import eu.vendeli.tgbot.types.ParseMode
import eu.vendeli.tgbot.types.internal.Response
import me.phantomx.pekonime.bot.BotM.groupChat
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.USER_ADMIN_ID
import me.phantomx.pekonime.bot.bot
import me.phantomx.pekonime.bot.extension.loadPrivateChatBotData
import me.phantomx.pekonime.bot.npGson
import me.phantomx.pekonime.bot.privateChatData
import org.slf4j.LoggerFactory

class AdminMessageController {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @UnprocessedHandler
    suspend fun onAdminReplyMessage(e: Event) {
        if (e.user.id != USER_ADMIN_ID) return

        val msg = e.fullUpdate.message ?: return
        val text = msg.text ?: return

        val replyTo = msg.replyToMessage?.messageId ?: return

        loadPrivateChatBotData()
        val data = privateChatData[replyTo] ?: return

        e.isCancelled = true

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
                }.send(e.user, bot)
        }

    }

    @UnprocessedHandler(priority = MethodPriority.LOWEST)
    suspend fun onAdminSendMessage(e: Event) {
        if (e.user.id != USER_ADMIN_ID || e.fullUpdate.message?.chat?.id != USER_ADMIN_ID) return
        if (!isAdminSendMessageGroup) return

        val text = e.fullUpdate.message?.text ?: return

        message(text).options {
            parseMode = ParseMode.MarkdownV2
        }.send(groupChat.id, e.bot)

    }

}