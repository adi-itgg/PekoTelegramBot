package me.phantomx.pekonime.bot.controller

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.UnprocessedHandler
import eu.vendeli.tgbot.api.chat.restrictChatMember
import eu.vendeli.tgbot.api.deleteMessage
import eu.vendeli.tgbot.api.message
import eu.vendeli.tgbot.types.ChatPermissions
import eu.vendeli.tgbot.types.ParseMode
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import eu.vendeli.tgbot.utils.inlineKeyboardMarkup
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig
import me.phantomx.pekonime.bot.extension.GET
import me.phantomx.pekonime.bot.extension.mention

class JoinLeaveController {

    @UnprocessedHandler
    suspend fun onJoinLeave(update: ProcessedUpdate, bot: TelegramBot) {
        val msg = update.fullUpdate.message ?: return

        // remove join & leave message
        val isJoin = msg.newChatMembers?.isNotEmpty() == true
        val isLeave = msg.leftChatMember != null

        if (isJoin || isLeave)
            deleteMessage(msg.messageId).send(BuildConfig.BOT_GROUP_ID, bot)

        if (isJoin)
            message {
                BuildConfig.MESSAGE_GROUP_WELCOME.GET.format(update.user.mention)
            }.options {
                parseMode = ParseMode.HTML
            }.markup {
                inlineKeyboardMarkup {
                    url(BuildConfig.BUTTON_START) {
                        BuildConfig.TELEGRAM_ME + BuildConfig.BOT_ID
                    }
                }
            }.send(msg.chat.id, bot)

        // deny new member chat permission until /start command performed
        msg.newChatMembers?.forEach {
            restrictChatMember(
                userId = it.id,
                chatPermissions = ChatPermissions(
                    canSendMessages = false
                ),
                untilDate = 0
            ).send(BuildConfig.BOT_GROUP_ID, bot)
        }
    }

}