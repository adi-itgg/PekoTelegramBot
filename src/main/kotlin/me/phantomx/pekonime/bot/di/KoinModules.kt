package me.phantomx.pekonime.bot.di

import me.phantomx.pekonime.bot.controller.BotController
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

val controllerModules = module {
    single { BotController() }
}

fun inject() = startKoin {
    modules(controllerModules)
}