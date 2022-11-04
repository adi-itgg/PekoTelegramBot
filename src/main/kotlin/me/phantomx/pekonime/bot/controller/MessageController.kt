package me.phantomx.pekonime.bot.controller

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.UnprocessedHandler
import eu.vendeli.tgbot.api.deleteMessage
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import eu.vendeli.tgbot.types.internal.UpdateType
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildResources
import me.phantomx.pekonime.bot.blockedWords
import me.phantomx.pekonime.bot.extension.GET
import me.phantomx.pekonime.bot.extension.mention
import me.phantomx.pekonime.bot.extension.messageHtml

class MessageController {

    @UnprocessedHandler
    suspend fun blockBadWords(update: ProcessedUpdate, bot: TelegramBot) {
        // skip if not message
        if (update.type != UpdateType.MESSAGE) return

        val msg = update.fullUpdate.message?.text?.lowercase() ?: return

        if (blockedWords.isEmpty())
            blockedWords = BuildResources.BLOCKED_WORDS_TXT.readText().split("\n").toList()

        blockedWords.forEach {
            if (!msg.contains(it)) return@forEach
            // if contains
            deleteMessage(update.fullUpdate.message?.messageId ?: 0).send(BuildConfig.BOT_GROUP_ID, bot)
            messageHtml(update.user) {
                BuildConfig.MESSAGE_WARN_TOXIC.GET.format(update.user.mention)
            }
        }
    }

}