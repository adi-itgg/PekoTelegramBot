package me.phantomx.pekonime.bot

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.enums.HttpLogLevel
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.BOT_TOKEN
import me.phantomx.pekonime.bot.controller.CmdController

val bot = TelegramBot.Builder(BOT_TOKEN) {
    controllersPackage = CmdController::class.java.packageName
    httpLogLevel = HttpLogLevel.NONE
}.build()


var blockedWords = listOf<String>()
val cacheData = mutableMapOf<String, String>()