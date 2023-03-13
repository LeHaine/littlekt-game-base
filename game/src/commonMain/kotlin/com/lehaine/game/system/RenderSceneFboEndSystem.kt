package com.lehaine.game.system

import com.github.quillraven.fleks.IntervalSystem
import com.lehaine.littlekt.graphics.FrameBuffer
import com.lehaine.littlekt.graphics.g2d.Batch

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
class RenderSceneFboEndSystem(
    private val batch: Batch,
    private var fbo: FrameBuffer
) : IntervalSystem() {

    fun setFbo(fbo: FrameBuffer) {
        this.fbo = fbo
    }

    override fun onTick() {
        batch.end()
        fbo.end()
    }
}