package me.phantomx.pekonime.bot.controller

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.UnprocessedHandler
import eu.vendeli.tgbot.api.deleteMessage
import eu.vendeli.tgbot.api.message
import eu.vendeli.tgbot.types.ParseMode
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import eu.vendeli.tgbot.types.internal.UpdateType
import me.phantomx.pekonime.bot.BotM.groupChat
import me.phantomx.pekonime.bot.BotM.isInitializedGroupChat
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.MESSAGE_WARN_TOXIC
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildResources.BLOCKED_WORDS_TXT
import me.phantomx.pekonime.bot.blockedWords
import me.phantomx.pekonime.bot.utils.mention

class MessageController {

    @UnprocessedHandler
    suspend fun blockBadWords(update: ProcessedUpdate, bot: TelegramBot) {
        if (!isInitializedGroupChat) return
        // skip if not message
        if (update.type != UpdateType.MESSAGE) return

        val msg = update.fullUpdate.message?.text?.lowercase() ?: return

        if (blockedWords.isEmpty())
            blockedWords = BLOCKED_WORDS_TXT.get.split("\n").toList()

        blockedWords.forEach {
            if (!msg.contains(it) || it.isEmpty()) return@forEach
            // if contains
            deleteMessage(update.fullUpdate.message?.messageId ?: 0).send(groupChat.id, bot)
            message {
                MESSAGE_WARN_TOXIC.format(update.user.mention)
            }.options {
                parseMode = ParseMode.HTML
            }.send(update.user, bot)
        }
    }

}