package me.phantomx.pekonime.bot.controller

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.annotations.UnprocessedHandler
import eu.vendeli.tgbot.api.chat.*
import eu.vendeli.tgbot.api.deleteMessage
import eu.vendeli.tgbot.api.message
import eu.vendeli.tgbot.types.ChatPermissions
import eu.vendeli.tgbot.types.ParseMode
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import eu.vendeli.tgbot.types.internal.UpdateType
import eu.vendeli.tgbot.utils.inlineKeyboardMarkup
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.BOT_GROUP
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.BOT_GROUP_ID
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.BUTTON_START
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.MESSAGE_WARN_TOXIC
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.MESSAGE_WELCOME
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.TELEGRAM_ME
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildResources
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildResources.RULES_HTML
import me.phantomx.pekonime.bot.blockedWords
import me.phantomx.pekonime.bot.cacheData
import me.phantomx.pekonime.bot.extension.GET
import me.phantomx.pekonime.bot.extension.mention
import me.phantomx.pekonime.bot.extension.messageHtml
import org.slf4j.LoggerFactory

@Suppress("unused")
class BotController: UnhandledController() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @UnprocessedHandler
    suspend fun unhandled(update: ProcessedUpdate, bot: TelegramBot) {
        onJoinLeave(update, bot)
        onMessageReceived(update, bot)
    }

    @CommandHandler(["/start", "/mulai"])
    suspend fun start(update: ProcessedUpdate, bot: TelegramBot) {
        // allow user send message to group
        restrictChatMember(
            userId = update.user.id,
            chatPermissions = ChatPermissions(
                canSendMessages = true
            ),
            untilDate = 0
        ).send(BOT_GROUP_ID, bot)

        logger.info("Permission guaranteed: ${update.user.id} - ${update.user.firstName}")

        // send back to group
        message {
            MESSAGE_WELCOME.GET.format(update.user.mention)
        }.options {
            parseMode = ParseMode.HTML
        }.markup {
            inlineKeyboardMarkup {
                url(BUTTON_START) {
                    TELEGRAM_ME + BOT_GROUP
                }
            }
        }.send(update.fullUpdate.message?.chat?.id ?: update.user.id, bot)
    }

    @CommandHandler(["/rules", "/peraturan"])
    suspend fun rules(update: ProcessedUpdate, bot: TelegramBot) {
        val content = cacheData[RULES_HTML.name] ?: RULES_HTML.readText().apply {
            cacheData[RULES_HTML.name] = this
        }

        messageHtml(update.user, content)
    }

}