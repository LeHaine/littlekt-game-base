package com.lehaine.game

import com.lehaine.littlekt.createLittleKtApp
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.HdpiMode

fun main() {
    createLittleKtApp {
        width = 960
        height = 540
        backgroundColor = Color.DARK_GRAY
        title = "LittleKt Game Base"
        val isMac = System.getProperty("os.name").lowercase().contains("mac")
        if (isMac) {
            hdpiMode = HdpiMode.PIXELS
        }
    }.start {
        GameCore(it)
    }
}