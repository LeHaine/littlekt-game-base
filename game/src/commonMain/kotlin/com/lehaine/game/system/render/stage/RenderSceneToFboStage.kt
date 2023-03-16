package com.lehaine.game.system.render.stage

import com.lehaine.game.system.render.RenderPipeline
import com.lehaine.game.system.render.RenderStage
import com.lehaine.littlekt.Context
import com.lehaine.littlekt.graphics.FrameBuffer
import com.lehaine.littlekt.graphics.GL
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.g2d.use
import com.lehaine.littlekt.graphics.gl.ClearBufferMask
import com.lehaine.littlekt.graphics.use
import com.lehaine.littlekt.util.fastForEach
import com.lehaine.littlekt.util.viewport.Viewport

/**
 * @author Colton Daily
 * @date 3/15/2023
 */
class RenderSceneToFboStage(
    private val context: Context,
    private val batch: Batch,
    private var sceneFbo: FrameBuffer,
    private val sceneViewport: Viewport,
    override val stages: List<RenderStage>
) : RenderPipeline {
    private val gl: GL get() = context.gl

    fun updateFbo(newFbo: FrameBuffer) {
        sceneFbo = newFbo
    }

    override fun render() {
        sceneFbo.use {
            gl.clear(ClearBufferMask.COLOR_BUFFER_BIT)
            batch.useDefaultShader()
            sceneViewport.apply(context)
            batch.use(sceneViewport.camera.viewProjection) {
                stages.fastForEach { it.render() }
            }
        }
    }
}