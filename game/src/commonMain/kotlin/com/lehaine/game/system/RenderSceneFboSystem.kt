package com.lehaine.game.system

import com.github.quillraven.fleks.IntervalSystem
import com.lehaine.game.GridEntityCamera
import com.lehaine.littlekt.Context
import com.lehaine.littlekt.extras.graphics.PixelSmoothFrameBuffer
import com.lehaine.littlekt.extras.shader.PixelSmoothFragmentShader
import com.lehaine.littlekt.extras.shader.PixelSmoothVertexShader
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.g2d.TextureSlice
import com.lehaine.littlekt.graphics.g2d.use
import com.lehaine.littlekt.graphics.shader.ShaderProgram
import com.lehaine.littlekt.util.viewport.Viewport

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
class RenderSceneFboSystem(
    private val context: Context,
    private val batch: Batch,
    private var sceneFbo: PixelSmoothFrameBuffer,
    private var sceneFboSlice: TextureSlice,
    private val shader: ShaderProgram<PixelSmoothVertexShader, PixelSmoothFragmentShader>,
    private val sceneCamera: GridEntityCamera,
    private val viewport: Viewport,
) : IntervalSystem() {

    fun updateFboAndSlice(newFbo: PixelSmoothFrameBuffer, newSlice: TextureSlice) {
        sceneFbo = newFbo
        sceneFboSlice = newSlice
    }

    override fun onTick() {
        viewport.apply(context)
        batch.shader = shader
        batch.use(viewport.camera.viewProjection) {
            shader.vertexShader.uTextureSizes.apply(
                shader,
                sceneFbo.width.toFloat(),
                sceneFbo.height.toFloat(),
                0f,
                0f
            )
            shader.vertexShader.uSampleProperties.apply(
                shader, 0f, 0f, sceneCamera.scaledDistX, sceneCamera.scaledDistY
            )
            batch.draw(
                sceneFboSlice,
                0f,
                0f,
                width = context.graphics.width.toFloat(),
                height = context.graphics.height.toFloat(),
                flipY = true
            )
        }
    }
}