package com.lehaine.game.system

import com.github.quillraven.fleks.IntervalSystem
import com.lehaine.game.GridEntityCamera
import com.lehaine.littlekt.math.Rect
import com.lehaine.littlekt.util.calculateViewBounds
import com.lehaine.littlekt.util.seconds

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
class CameraSystem(private val camera: GridEntityCamera, private val viewBounds: Rect) : IntervalSystem() {

    override fun onTick() {
        viewBounds.calculateViewBounds(camera)
        camera.update(deltaTime.seconds)
    }
}