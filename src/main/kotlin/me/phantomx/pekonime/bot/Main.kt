package me.phantomx.pekonime.bot

import kotlinx.coroutines.runBlocking
import me.phantomx.pekonime.bot.extension.registerMyCommands

val isTesting = false

fun main(): Unit = runBlocking {
    registerMyCommands()
    bot.handleUpdates()
}
