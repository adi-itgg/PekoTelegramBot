package me.phantomx.pekonime.bot.utils

import java.io.File

class FileChecker(path: String) {

    private val file = File(path)

    private var lastModified = -1L
    private var content = ""

    private fun createDirs() {
        file.parentFile?.apply {
            if (exists()) return@apply
            mkdirs()
            mkdir()
        }
    }

    private fun check() {
        if (lastModified == file.lastModified()) return
        createDirs()
        if (!file.exists())
            file.writeText("")
        else
            content = file.readText()
        lastModified = file.lastModified()
    }

    fun get(): String {
        check()
        return content
    }

    fun write(text: String) {
        content = text
        createDirs()
        file.writeText(text)
        lastModified = file.lastModified()
    }

}