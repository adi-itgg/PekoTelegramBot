package me.phantomx.pekonime.bot

import kotlinx.coroutines.runBlocking
import me.phantomx.pekonime.bot.di.inject
import me.phantomx.pekonime.bot.extension.myCommands

fun main(): Unit = runBlocking {
    inject()
    myCommands()
    bot.handleUpdates()
}