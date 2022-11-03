package me.phantomx.pekonime.bot

import eu.vendeli.tgbot.TelegramBot
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.BOT_TOKEN
import me.phantomx.pekonime.bot.configuration.KoinClassManager
import me.phantomx.pekonime.bot.controller.BotController

val bot = TelegramBot(
    token = BOT_TOKEN,
    commandsPackage = BotController::class.java.packageName,
    classManager = KoinClassManager()
)


var blockedWords = listOf<String>()
val cacheData = mutableMapOf<String, String>()