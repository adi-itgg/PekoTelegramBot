package me.phantomx.pekonime.bot.controller

import eu.vendeli.tgbot.annotations.UnprocessedHandler
import eu.vendeli.tgbot.interfaces.Event
import me.phantomx.pekonime.bot.BotM.groupChat
import me.phantomx.pekonime.bot.BotM.isInitializedGroupChat
import me.phantomx.pekonime.bot.PekoTelegramBot.BuildConfig.USER_ADMIN_ID
import me.phantomx.pekonime.bot.extension.notifyAdminMessage

class CatchUserPrivateChatController {

    @UnprocessedHandler
    suspend fun onPrivateChat(e: Event) {
        if (e.user.id == USER_ADMIN_ID || !isInitializedGroupChat) return
        val msg = e.fullUpdate.message ?: return
        if (msg.chat.id == groupChat.id) return

        if (msg.text?.startsWith("/") == true) return

        e.notifyAdminMessage()

        e.isHandled = true
    }

}