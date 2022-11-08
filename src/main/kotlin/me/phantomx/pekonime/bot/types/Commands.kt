package me.phantomx.pekonime.bot.types

class Commands(vararg args: Pair<String, String>) {

    val commands: Map<String, String>

    init {
        val data = mutableMapOf<String, String>()
        args.forEach {
            data[it.first] = it.second
        }
        commands = data
    }

}