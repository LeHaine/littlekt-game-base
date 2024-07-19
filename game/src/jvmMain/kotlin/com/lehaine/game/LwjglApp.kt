package com.lehaine.game

import com.littlekt.createLittleKtApp
import com.littlekt.graphics.Color
import com.littlekt.graphics.HdpiMode

fun main() {
    createLittleKtApp {
        width = 960
        height = 540
        title = "LittleKt Game Base"
        val isMac = System.getProperty("os.name").lowercase().contains("mac")
        if (isMac) {
            hdpiMode = HdpiMode.PIXELS
        }
    }.start {
        GameCore(it)
    }
}