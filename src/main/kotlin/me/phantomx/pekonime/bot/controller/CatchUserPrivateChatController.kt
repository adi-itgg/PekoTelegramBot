package me.phantomx.pekonime.bot.controller

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.UnprocessedHandler
import eu.vendeli.tgbot.api.message
import eu.vendeli.tgbot.interfaces.sendAsync
import eu.vendeli.tgbot.types.ParseMode
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import eu.vendeli.tgbot.types.internal.Response
import me.phantomx.pekonime.bot.BotM.groupChat
import me.phantomx.pekonime.bot.BotM.isInitializedGroupChat
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.USER_ADMIN_ID
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildResources.DATA_BOT_PC_DATA_JSON
import me.phantomx.pekonime.bot.data.BotPrivateChatData
import me.phantomx.pekonime.bot.extension.loadPrivateChatBotData
import me.phantomx.pekonime.bot.extension.mention
import me.phantomx.pekonime.bot.gson
import me.phantomx.pekonime.bot.privateChatData
import org.slf4j.LoggerFactory

class CatchUserPrivateChatController {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @UnprocessedHandler
    suspend fun onPrivateChat(update: ProcessedUpdate, bot: TelegramBot) {
        if (update.user.id == USER_ADMIN_ID || !isInitializedGroupChat) return
        val msg = update.fullUpdate.message ?: return
        if (msg.chat.id == groupChat.id) return

        val text = msg.text ?: return

        if (text.startsWith("/")) return

        when (val res = message {
            "$text\n<pre>by</pre> ${update.user.mention}"
        }.options {
            parseMode = ParseMode.HTML
        }.sendAsync(USER_ADMIN_ID, bot).await()) {
            is Response.Failure -> {
                logger.error("get message response failure!")
            }
            is Response.Success -> {
                loadPrivateChatBotData()
                privateChatData[res.result.messageId] = BotPrivateChatData(
                    text = text,
                    userId = update.user.id,
                    chatId = msg.chat.id,
                    replyMsgId = msg.messageId
                )
                DATA_BOT_PC_DATA_JSON.write(gson.toJson(privateChatData))
            }
        }

    }

}