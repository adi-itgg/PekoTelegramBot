package me.phantomx.pekonime.bot

import eu.vendeli.tgbot.api.botactions.getMe
import eu.vendeli.tgbot.interfaces.sendAsync
import eu.vendeli.tgbot.types.Chat
import eu.vendeli.tgbot.types.internal.onFailure
import eu.vendeli.tgbot.types.internal.onSuccess
import kotlinx.coroutines.runBlocking
import me.phantomx.pekonime.bot.BotM.groupChat
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.BOT_TOKEN
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildResources.DATA_BOT_DATA_JSON
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildResources.DATA_GROUP_JSON
import me.phantomx.pekonime.bot.data.BotProfile
import me.phantomx.pekonime.bot.extension.registerMyCommands
import me.phantomx.pekonime.bot.utils.toClassObj
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

private lateinit var meProfile: BotProfile
val botProfile: BotProfile
    get() = meProfile

object BotM {
    lateinit var groupChat: Chat
    val isInitializedGroupChat: Boolean
        get() = this::groupChat.isInitialized
}

fun main(): Unit = runBlocking {

    val logger = LoggerFactory.getLogger("Main")


    DATA_BOT_DATA_JSON.get().toClassObj<Map<String, BotProfile>> {
        it.isNotEmpty()
    }?.get(BOT_TOKEN)?.let {
        meProfile = it
    } ?: run {
        logger.debug("Getting bot profile data...")
        getMe().sendAsync(bot).await().onSuccess { response ->
            meProfile = BotProfile(response.result)
            DATA_BOT_DATA_JSON.get().toClassObj<MutableMap<String, BotProfile>> {
                it.isNotEmpty()
            }?.let {
                it[BOT_TOKEN] = meProfile
                DATA_BOT_DATA_JSON.write(gson.toJson(it))
            } ?: run {
                DATA_BOT_DATA_JSON.write(gson.toJson(mapOf(BOT_TOKEN to meProfile)))
            }
        }.onFailure {
            logger.error("${it.errorCode}: ${it.description}")
            exitProcess(30)
        }
    }

    DATA_GROUP_JSON.get().toClassObj<Chat> {
        it.id != 0L
    }?.let {
        groupChat = it
        logger.info("Bot active in group ${groupChat.title}")
    } ?: logger.warn("Require /setgroup in group chat!")


    registerMyCommands()

    bot.handleUpdates()

}
