package com.lehaine.game.system.render.pipeline

import com.lehaine.game.GridEntityCamera
import com.lehaine.game.system.render.RenderPipeline
import com.lehaine.game.system.render.RenderStage
import com.lehaine.littlekt.extras.shader.PixelSmoothCameraSpriteShader
import com.littlekt.Context
import com.littlekt.graphics.g2d.Batch
import com.littlekt.graphics.g2d.TextureSlice
import com.littlekt.graphics.webgpu.CommandEncoder
import com.littlekt.util.datastructure.fastForEach
import com.littlekt.util.viewport.ScreenViewport

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
class RenderScenePipeline(
    context: Context,
    private val sceneCamera: GridEntityCamera,
    stages: List<RenderStage> = emptyList()
) : RenderPipeline(context, stages, "Render Scene Pipeline") {
    private val pixelSmoothShader = PixelSmoothCameraSpriteShader(context.graphics.device)
    private val screenViewport = ScreenViewport(context.graphics.width, context.graphics.height)

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        screenViewport.update(width, height, true)
    }

    override fun render(
        batch: Batch,
        commandEncoder: CommandEncoder,
        nextRenderTargetSlice: TextureSlice?
    ): TextureSlice {
        check(nextRenderTargetSlice != null) { "RenderScenePipeline requires nextRenderTarget slice!" }
        batch.shader = pixelSmoothShader
        screenViewport.apply()
        val sceneRenderPass = commandEncoder.beginRenderPass(renderTargetPassDescriptor)
        sceneRenderPass.setViewport(screenViewport.x, screenViewport.y, screenViewport.width, screenViewport.height)
        batch.viewProjection = screenViewport.camera.viewProjection
        pixelSmoothShader.updateTextureSize(
            nextRenderTargetSlice.texture.width.toFloat(),
            nextRenderTargetSlice.texture.height.toFloat(),
            sceneCamera.renderTarget?.upscale?.toFloat() ?: 1f
        )
        pixelSmoothShader.updateSampleProperties(
            sceneCamera.subpixelX,
            sceneCamera.subpixelY,
            sceneCamera.scaledDistX,
            sceneCamera.scaledDistY
        )
        batch.draw(
            nextRenderTargetSlice,
            0f,
            0f,
            width = context.graphics.width.toFloat(),
            height = context.graphics.height.toFloat(),
        )
        stages.fastForEach { stage ->
            when (stage) {
                is RenderStage.BatchStage -> stage.render(batch, commandEncoder, renderTargetPassDescriptor)
            }
        }
        batch.flush(sceneRenderPass)
        sceneRenderPass.end()
        sceneRenderPass.release()
        return renderTargetSlice
    }
}