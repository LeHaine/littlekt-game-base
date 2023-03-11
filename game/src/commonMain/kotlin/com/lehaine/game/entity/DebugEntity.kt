package com.lehaine.game.entity

import com.lehaine.game.Assets
import com.lehaine.game.Config
import com.lehaine.littlekt.Context
import com.lehaine.littlekt.extras.grid.entity.GridEntity
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.input.Key
import kotlin.time.Duration

/**
 * @author Colton Daily
 * @date 3/1/2023
 */
class DebugEntity(val context: Context) : GridEntity(Config.GRID_CELL_SIZE_F) {

    private var xDir = 0
    private var yDir = 0

    init {
        sprite.slice = Assets.atlas.getByPrefix("fxPixel").slice
        sprite.color = Color.YELLOW.toMutableColor()
        scaleX = 10f
        scaleY = 10f
    }

    override fun update(dt: Duration) {
        super.update(dt)
        xDir = 0
        yDir = 0
        if (context.input.isKeyPressed(Key.W)) {
            yDir = -1
        }
        if (context.input.isKeyPressed(Key.S)) {
            yDir = 1
        }
        if (context.input.isKeyPressed(Key.D)) {
            xDir = 1
        }
        if (context.input.isKeyPressed(Key.A)) {
            xDir = -1
        }
        if (context.input.isKeyJustPressed(Key.ENTER)) {
            stretchX = 2f
        }

        dir = if (xDir != 0) xDir else dir
    }

    override fun fixedUpdate() {
        super.fixedUpdate()
        velocityX += 0.12f * xDir
        velocityY += 0.12f * yDir
    }
}