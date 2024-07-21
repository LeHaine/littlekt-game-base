package com.lehaine.game.system.render.stage

import com.lehaine.game.GridEntityCamera
import com.lehaine.game.system.render.RenderPipeline
import com.lehaine.game.system.render.RenderStage
import com.lehaine.littlekt.extras.graphics.PixelSmoothRenderTarget
import com.lehaine.littlekt.extras.shader.PixelSmoothCameraSpriteShader
import com.littlekt.Context
import com.littlekt.graphics.g2d.Batch
import com.littlekt.graphics.g2d.TextureSlice
import com.littlekt.graphics.webgpu.CommandEncoder
import com.littlekt.graphics.webgpu.RenderPassDescriptor
import com.littlekt.util.datastructure.fastForEach
import com.littlekt.util.viewport.Viewport

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
class RenderSceneStage(
    private val context: Context,
    private var sceneRenderTarget: PixelSmoothRenderTarget,
    private var sceneRenderTargetSlice: TextureSlice,
    private val sceneCamera: GridEntityCamera,
    private val screenViewport: Viewport,
    override val stages: List<RenderStage> = emptyList()
) : RenderPipeline {

    private val pixelSmoothShader = PixelSmoothCameraSpriteShader(context.graphics.device)

    fun updateRenderTargetAndSlice(newRenderTarget: PixelSmoothRenderTarget, newSlice: TextureSlice) {
        sceneRenderTarget = newRenderTarget
        sceneRenderTargetSlice = newSlice
    }

    override fun render(batch: Batch, commandEncoder: CommandEncoder, renderPassDescriptor: RenderPassDescriptor) {
        batch.shader = pixelSmoothShader
        screenViewport.apply()
        val sceneRenderPass = commandEncoder.beginRenderPass(renderPassDescriptor)
        sceneRenderPass.setViewport(screenViewport.x, screenViewport.y, screenViewport.width, screenViewport.height)
        batch.viewProjection = screenViewport.camera.viewProjection
        pixelSmoothShader.updateTextureSize(sceneRenderTarget.width.toFloat(), sceneRenderTarget.height.toFloat())
        pixelSmoothShader.updateSampleProperties(sceneCamera.scaledDistX, sceneCamera.scaledDistY)
        batch.draw(
            sceneRenderTargetSlice,
            0f,
            0f,
            width = context.graphics.width.toFloat(),
            height = context.graphics.height.toFloat(),
        )
        stages.fastForEach { it.render(batch, commandEncoder, renderPassDescriptor) }
        batch.flush(
            sceneRenderPass
        )
        sceneRenderPass.end()
        sceneRenderPass.release()
    }
}