package me.phantomx.pekonime.bot.controller

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.UnprocessedHandler
import eu.vendeli.tgbot.api.chat.restrictChatMember
import eu.vendeli.tgbot.api.deleteMessage
import eu.vendeli.tgbot.api.message
import eu.vendeli.tgbot.types.ParseMode
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import eu.vendeli.tgbot.utils.inlineKeyboardMarkup
import me.phantomx.pekonime.bot.BotM.groupChat
import me.phantomx.pekonime.bot.BotM.isInitializedGroupChat
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.INLINE_BUTTON_START
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.MESSAGE_GROUP_WELCOME
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.TELEGRAM_ME
import me.phantomx.pekonime.bot.botProfile
import me.phantomx.pekonime.bot.utils.mention

class JoinLeaveController {

    @UnprocessedHandler
    suspend fun onJoinLeave(update: ProcessedUpdate, bot: TelegramBot) {
        if (!isInitializedGroupChat) return
        val msg = update.fullUpdate.message ?: return

        // remove join & leave message
        val isJoin = msg.newChatMembers?.isNotEmpty() == true
        val isLeave = msg.leftChatMember != null

        if (isJoin || isLeave)
            deleteMessage(msg.messageId).send(groupChat.id, bot)

        if (isJoin)
            message {
                MESSAGE_GROUP_WELCOME.format(update.user.mention)
            }.options {
                parseMode = ParseMode.HTML
            }.markup {
                inlineKeyboardMarkup {
                    INLINE_BUTTON_START url TELEGRAM_ME + botProfile.username
                }
            }.send(msg.chat.id, bot)

        // deny new member chat permission until /start command performed
        msg.newChatMembers?.forEach {
            restrictChatMember(
                userId = it.id,
                untilDate = 0
            ) {
                canSendMessages = false
            }.send(groupChat.id, bot)
        }
    }

}