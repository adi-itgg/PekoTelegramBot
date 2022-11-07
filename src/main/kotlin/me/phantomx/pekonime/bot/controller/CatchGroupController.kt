package me.phantomx.pekonime.bot.controller

import eu.vendeli.tgbot.annotations.UnprocessedHandler
import eu.vendeli.tgbot.enums.MethodPriority
import eu.vendeli.tgbot.interfaces.Event
import eu.vendeli.tgbot.types.EntityType
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.USER_ADMIN_ID
import me.phantomx.pekonime.bot.botProfile
import me.phantomx.pekonime.bot.utils.notifyAdminMessage
import org.slf4j.LoggerFactory

class CatchGroupController {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @UnprocessedHandler(priority = MethodPriority.LOW)
    suspend fun catchMention(e: Event) {
        if (e.user.id == USER_ADMIN_ID) return
        val msg = e.fullUpdate.message ?: return

        val text = msg.text ?: return
        if (!text.contains(botProfile.username) || text.startsWith("/")) return

        val entities = msg.entities ?: return

        if (entities.none { it.type == EntityType.Mention }) return

        e.notifyAdminMessage()

    }

    @UnprocessedHandler
    suspend fun catchReplyMessageBot(e: Event) {
        if (e.user.id == USER_ADMIN_ID) return
        val msg = e.fullUpdate.message ?: return

        msg.text ?: return

        // is bot message
        val replyMsg = msg.replyToMessage ?: return
        val id = replyMsg.from?.username ?: return

        if (id != botProfile.username) return

        e.isCancelled = true

        e.notifyAdminMessage()

    }

}