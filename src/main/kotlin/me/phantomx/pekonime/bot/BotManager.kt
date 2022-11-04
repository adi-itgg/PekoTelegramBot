package me.phantomx.pekonime.bot

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.enums.HttpLogLevel
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.BOT_TOKEN
import me.phantomx.pekonime.bot.controller.CmdController
import me.phantomx.pekonime.bot.data.BotPrivateChatData

val bot = TelegramBot.Builder(BOT_TOKEN) {
    controllersPackage = CmdController::class.java.packageName
    httpLogLevel = HttpLogLevel.NONE
}.build()

val gson: Gson = GsonBuilder().setPrettyPrinting().create()
val npGson = Gson()


var blockedWords = listOf<String>()

var privateChatData = mutableMapOf<Long, BotPrivateChatData>()