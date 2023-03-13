package com.lehaine.game.system

import com.github.quillraven.fleks.IntervalSystem
import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.math.Rect
import com.lehaine.littlekt.util.calculateViewBounds

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
class CameraViewBoundsCalculatorSystem(private val camera: Camera, private val viewBounds: Rect) : IntervalSystem() {

    override fun onTick() {
        viewBounds.calculateViewBounds(camera)
    }
}