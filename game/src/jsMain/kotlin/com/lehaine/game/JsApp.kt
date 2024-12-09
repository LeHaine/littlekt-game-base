package com.lehaine.game

import com.littlekt.createLittleKtApp
import com.littlekt.graphics.Color
import kotlinx.browser.window

fun main() {
    createLittleKtApp {
        title = "LittleKt Game Template"
        canvasId = "canvas"
    }.start {
        GameCore(it)
    }
}