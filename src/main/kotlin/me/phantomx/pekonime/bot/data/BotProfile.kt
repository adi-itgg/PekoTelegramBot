package me.phantomx.pekonime.bot.data

import eu.vendeli.tgbot.types.User

class BotProfile(
    val user: User
) {

    val username: String
        get() = user.username!!

}
