package com.lehaine.game

import com.littlekt.createLittleKtApp
import com.littlekt.graphics.Color
import kotlinx.browser.window

fun main() {
    createLittleKtApp {
        title = "LittleKt Game Template"
        backgroundColor = Color.DARK_GRAY
        canvasId = "canvas"
    }.start {
        GameCore(it)
    }
}