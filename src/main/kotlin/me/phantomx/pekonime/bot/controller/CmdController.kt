package me.phantomx.pekonime.bot.controller

import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.answerCallbackQuery
import eu.vendeli.tgbot.api.chat.*
import eu.vendeli.tgbot.api.deleteMessage
import eu.vendeli.tgbot.api.message
import eu.vendeli.tgbot.interfaces.Event
import eu.vendeli.tgbot.interfaces.sendAsync
import eu.vendeli.tgbot.types.ChatPermissions
import eu.vendeli.tgbot.types.ParseMode
import eu.vendeli.tgbot.types.internal.onSuccess
import eu.vendeli.tgbot.utils.inlineKeyboardMarkup
import kotlinx.coroutines.delay
import me.phantomx.pekonime.bot.BotM.groupChat
import me.phantomx.pekonime.bot.BotM.isInitializedGroupChat
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.BOT_GROUP
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.INLINE_BUTTON_RULES
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.INLINE_BUTTON_START
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.MESSAGE_GROUP_SET
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.MESSAGE_HELP
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.MESSAGE_NO_PERM
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.MESSAGE_WELCOME
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.TELEGRAM_ME
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.USER_ADMIN_ID
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildResources.DATA_GROUP_JSON
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildResources.RULES_HTML
import me.phantomx.pekonime.bot.bot
import me.phantomx.pekonime.bot.extension.GET
import me.phantomx.pekonime.bot.extension.mention
import me.phantomx.pekonime.bot.extension.messageHtml
import me.phantomx.pekonime.bot.extension.send
import me.phantomx.pekonime.bot.gson
import org.slf4j.LoggerFactory

@Suppress("unused")
class CmdController {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @CommandHandler(["/start", "/mulai"])
    suspend fun start(e: Event) {
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
            chatPermissions = ChatPermissions(
                canSendMessages = true
            ),
            untilDate = 0
        ).send(groupChat.id, bot)

        logger.info("Permission guaranteed: ${e.user.id} - ${e.user.firstName}")

        // send back to group
        message {
            MESSAGE_WELCOME.GET.format(e.user.mention)
        }.options {
            parseMode = ParseMode.HTML
        }.markup {
            inlineKeyboardMarkup {
                url(INLINE_BUTTON_START) {
                    TELEGRAM_ME + BOT_GROUP
                }
            }
        }.send(e)
    }

    @CommandHandler(["/rules", "/peraturan"])
    suspend fun rules(e: Event) {
        messageHtml(e, RULES_HTML.get())
        e.fullUpdate.callbackQuery?.let {
            answerCallbackQuery(it.id).send(e)
        }
    }

    @CommandHandler(["/gmsg", "/groupmessage"])
    suspend fun toggleAdminMessageGroup(e: Event) {
        if (e.user.id != USER_ADMIN_ID || e.fullUpdate.message?.chat?.id != USER_ADMIN_ID) {
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

    @CommandHandler(["/help", "/bantuan"])
    suspend fun help(e: Event) {
        message {
            MESSAGE_HELP
        }.markup {
            inlineKeyboardMarkup {
                INLINE_BUTTON_RULES callback "/rules"
            }
        }.send(e)
    }

    @CommandHandler(["/setgroup"])
    suspend fun setGroup(e: Event) {
        if (e.user.id != USER_ADMIN_ID && e.fullUpdate.message?.from?.isBot != true && e.fullUpdate.message?.senderChat?.id == groupChat.id) {
            message(MESSAGE_NO_PERM).send(e)
            return
        }
        groupChat = e.fullUpdate.message?.chat ?: return
        DATA_GROUP_JSON.write(gson.toJson(groupChat))
        logger.info("Bot active in group ${groupChat.title}")
        message {
            MESSAGE_GROUP_SET
        }.sendAsync(e.fullUpdate.message?.chat?.id ?: e.user.id, e.bot).await().onSuccess {
            delay(5_000)
            deleteMessage(e.fullUpdate.message?.messageId ?: 0).send(e)
            deleteMessage(it.result.messageId).send(e)
        }
    }

}