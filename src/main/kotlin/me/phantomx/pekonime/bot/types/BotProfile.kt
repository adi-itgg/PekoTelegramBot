package me.phantomx.pekonime.bot.types

import eu.vendeli.tgbot.types.User

class BotProfile(
    private val user: User
) {

    val username: String
        get() = user.username!!

}
