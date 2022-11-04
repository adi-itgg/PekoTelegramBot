package me.phantomx.pekonime.bot

import kotlinx.coroutines.runBlocking
import me.phantomx.pekonime.bot.extension.registerMyCommands

fun main(): Unit = runBlocking {
    registerMyCommands()
    bot.handleUpdates()
}
