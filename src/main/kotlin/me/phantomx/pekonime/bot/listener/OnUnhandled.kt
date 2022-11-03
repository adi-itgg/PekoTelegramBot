package me.phantomx.pekonime.bot.listener

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.types.internal.ProcessedUpdate

interface OnUnhandled {

    suspend fun onJoinLeave(update: ProcessedUpdate, bot: TelegramBot)

    suspend fun onMessageReceived(update: ProcessedUpdate, bot: TelegramBot)

}