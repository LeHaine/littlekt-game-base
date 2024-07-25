package com.lehaine.game

/**
 * @author Colton Daily
 * @date 3/1/2023
 */
object Config {
    const val FIXED_STEP_INTERVAL: Float = 1 / 30f
    const val PPU: Float = 1f

    const val VIRTUAL_WIDTH: Int = 480
    const val VIRTUAL_HEIGHT: Int = 270

    const val GRID_CELL_SIZE: Int = 16
    const val GRID_CELL_SIZE_F: Float = GRID_CELL_SIZE.toFloat()

    var keyboardType = KeyboardType.QWERTY

    enum class KeyboardType {
        QWERTY,
        AZERTY
    }
}
