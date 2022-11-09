package me.phantomx.pekonime.bot.controller

import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.answerCallbackQuery
import eu.vendeli.tgbot.api.chat.restrictChatMember
import eu.vendeli.tgbot.api.deleteMessage
import eu.vendeli.tgbot.api.message
import eu.vendeli.tgbot.interfaces.Event
import eu.vendeli.tgbot.interfaces.sendAsync
import eu.vendeli.tgbot.types.ParseMode
import eu.vendeli.tgbot.types.internal.onSuccess
import eu.vendeli.tgbot.utils.inlineKeyboardMarkup
import kotlinx.coroutines.delay
import me.phantomx.pekonime.bot.BotM.groupChat
import me.phantomx.pekonime.bot.BotM.isInitializedGroupChat
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.BOT_GROUP
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.INLINE_BUTTON_RULES
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.INLINE_BUTTON_START
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.MESSAGE_CMD_COOLDOWN
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.MESSAGE_GROUP_SET
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.MESSAGE_HELP
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.MESSAGE_NO_PERM
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.MESSAGE_WELCOME
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.TELEGRAM_ME
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.USER_ADMIN_ID
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildResources.DATA_GROUP_JSON
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildResources.RULES_HTML
import me.phantomx.pekonime.bot.bot
import me.phantomx.pekonime.bot.gson
import me.phantomx.pekonime.bot.utils.mention
import me.phantomx.pekonime.bot.utils.send
import me.phantomx.pekonime.bot.utils.sendAsync
import org.slf4j.LoggerFactory

@Suppress("unused")
class CmdController {

    private val logger = LoggerFactory.getLogger(this::class.java.simpleName)

    @CommandHandler(["/start", "/mulai"])
    suspend fun start(e: Event) {
        if (e.chatIsGroup) return
        if (!isInitializedGroupChat) {
            logger.warn("Require /setgroup in group chat!")
            return
        }

        if (e.user.id != e.fullUpdate.message?.chat?.id) {
            deleteMessage(e.fullUpdate.message?.messageId ?: 0).send(e)
            return
        }

        // allow user send message to group
        restrictChatMember(
            userId = e.user.id,
            untilDate = 0
        ) {
            canSendMessages = true
        }.send(groupChat.id, bot)

        logger.info("Permission guaranteed: ${e.user.id} - ${e.user.firstName}")

        // send back to group
        message {
            MESSAGE_WELCOME.format(e.user.mention)
        }.options {
            parseMode = ParseMode.HTML
        }.markup {
            inlineKeyboardMarkup {
                INLINE_BUTTON_START url TELEGRAM_ME + BOT_GROUP
            }
        }.send(e)
    }

    @CommandHandler(value = ["/rules", "/peraturan"], cooldown = 10_000, ignoreCooldown = true)
    suspend fun rules(e: Event) {
        e.fullUpdate.callbackQuery?.let {
            answerCallbackQuery(it.id).send(e)
        }

        if (e.isCooldown) {
            message {
                MESSAGE_CMD_COOLDOWN
            }.options {
                replyToMessageId = e.fullUpdate.message?.messageId
            }.sendAsync(e).await().onSuccess {
                delay(5_000)

                if (e.fullUpdate.message?.chat?.id == groupChat.id)
                    deleteMessage(e.fullUpdate.message?.messageId ?: 0).send(e)
                deleteMessage(it.result.messageId).send(e)
            }
            return
        }

        message {
            RULES_HTML.get
        }.options {
            parseMode = ParseMode.HTML
        }.sendAsync(e).await().onSuccess {
            if (e.chatId > 0) return@onSuccess
            delay(10_000)
            deleteMessage(it.result.messageId).send(e)
            deleteMessage(e.fullUpdate.message?.messageId ?: 0).send(e)
        }
    }

    @CommandHandler(["/gmsg", "/groupmessage"])
    suspend fun toggleAdminMessageGroup(e: Event) {
        if (e.chatIsGroup) return
        if (e.user.id != USER_ADMIN_ID || e.chatId != USER_ADMIN_ID) {
            message(MESSAGE_NO_PERM).send(e)
            return
        }

        isAdminSendMessageGroup = !isAdminSendMessageGroup

        message {
            "Admin message group is ${if (isAdminSendMessageGroup) "active" else "deactivated"}"
        }.options {
            replyToMessageId = e.fullUpdate.message?.messageId
        }.sendAsync(e.user.id, bot).await().onSuccess {
            delay(5_000)
            deleteMessage(it.result.messageId).send(e.user.id, bot)
        }
    }

    @CommandHandler(["/help", "/bantuan"], 10_000)
    suspend fun help(e: Event) {
        message {
            MESSAGE_HELP
        }.options {
            replyToMessageId = e.fullUpdate.message?.messageId
        }.markup {
            inlineKeyboardMarkup {
                INLINE_BUTTON_RULES callback "/rules"
            }
        }.send(e)
    }

    @CommandHandler(["/setgroup"])
    suspend fun setGroup(e: Event) {
        if (!e.chatIsGroup) return
        if (e.user.id != USER_ADMIN_ID && e.fullUpdate.message?.from?.isBot != true && e.fullUpdate.message?.senderChat?.id == groupChat.id) {
            message(MESSAGE_NO_PERM).send(e)
            return
        }
        groupChat = e.fullUpdate.message?.chat ?: return
        DATA_GROUP_JSON.write { gson.toJson(groupChat) }
        logger.info("Bot active in group ${groupChat.title}")
        message {
            MESSAGE_GROUP_SET
        }.options {
            replyToMessageId = e.fullUpdate.message?.messageId
        }.sendAsync(e.chatId, e.bot).await().onSuccess {
            delay(5_000)
            deleteMessage(it.result.messageId).send(e)
            deleteMessage(e.fullUpdate.message?.messageId ?: 0).send(e)
        }
    }

}