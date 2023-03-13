package com.lehaine.game.system

import com.github.quillraven.fleks.IntervalSystem
import com.lehaine.littlekt.Context
import com.lehaine.littlekt.graphics.FrameBuffer
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.gl.ClearBufferMask
import com.lehaine.littlekt.util.viewport.Viewport

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
class RenderSceneFboStartSystem(
    private val context: Context,
    private val batch: Batch,
    private var fbo: FrameBuffer,
    private val viewport: Viewport,
) : IntervalSystem() {

    fun setFbo(fbo: FrameBuffer) {
        this.fbo = fbo
    }

    override fun onTick() {
        viewport.apply(context)
        fbo.begin()
        context.gl.clear(ClearBufferMask.COLOR_BUFFER_BIT)
        batch.useDefaultShader()
        batch.begin(viewport.camera.viewProjection)
    }
}