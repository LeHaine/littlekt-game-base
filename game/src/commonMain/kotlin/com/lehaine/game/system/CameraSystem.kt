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

    private var tmod = 1f
    private val targetFPS = 60
    override fun onTick() {
        tmod = deltaTime * targetFPS
        viewBounds.calculateViewBounds(camera)
        camera.tmod = tmod
        camera.update(deltaTime.seconds)
    }
}