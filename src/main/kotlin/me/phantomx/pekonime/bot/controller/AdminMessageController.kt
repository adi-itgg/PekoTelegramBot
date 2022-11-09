package me.phantomx.pekonime.bot.controller

import eu.vendeli.tgbot.annotations.UnprocessedHandler
import eu.vendeli.tgbot.api.message
import eu.vendeli.tgbot.enums.MethodPriority
import eu.vendeli.tgbot.interfaces.Event
import eu.vendeli.tgbot.interfaces.sendAsync
import eu.vendeli.tgbot.types.ParseMode
import eu.vendeli.tgbot.types.internal.Response
import me.phantomx.pekonime.bot.BotM.groupChat
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.MESSAGE_REPLY_FAILED
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.MESSAGE_REPLY_SUCCESS
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.USER_ADMIN_ID
import me.phantomx.pekonime.bot.bot
import me.phantomx.pekonime.bot.types.loadPrivateChatBotData
import me.phantomx.pekonime.bot.npGson
import me.phantomx.pekonime.bot.privateChatData
import org.slf4j.LoggerFactory

class AdminMessageController {

    private val logger = LoggerFactory.getLogger(this::class.java.simpleName)

    @UnprocessedHandler
    suspend fun onAdminReplyMessage(e: Event) {
        if (e.user.id != USER_ADMIN_ID) return

        val msg = e.fullUpdate.message ?: return
        var text = msg.text ?: msg.caption ?: return

        val replyTo = msg.replyToMessage?.messageId ?: return

        loadPrivateChatBotData()
        val data = privateChatData[replyTo] ?: return

        e.isCancelled = true

        val dontReplyMsg = text.startsWith("!nr")
        if (dontReplyMsg) {
            text = text.substring(3)
            if (text.startsWith(" ")) text = text.substring(1)
        }

        when(val res = message {
            text
        }.options {
            parseMode = ParseMode.HTML
            if (!dontReplyMsg)
                replyToMessageId = data.replyMsgId
        }.sendAsync(data.chatId, bot).await()) {
            is Response.Failure -> logger.error("Failure send message to reply - ${npGson.toJson(data)}")
            is Response.Success -> // the data in privateChatData should be deleted to reduce storage
                message {
                    if (res.ok) MESSAGE_REPLY_SUCCESS else MESSAGE_REPLY_FAILED
                }.options {
                    replyToMessageId = msg.messageId
                }.send(e.user, bot)
        }

    }

    @UnprocessedHandler(priority = MethodPriority.LOW)
    suspend fun onAdminSendMessage(e: Event) {
        if (e.user.id != USER_ADMIN_ID || e.fullUpdate.message?.chat?.id != USER_ADMIN_ID) return
        if (!isAdminSendMessageGroup) return

        val text = e.fullUpdate.message?.text ?: return

        message(text).options {
            parseMode = ParseMode.HTML
        }.send(groupChat.id, e.bot)

        e.isHandled = true

    }

}