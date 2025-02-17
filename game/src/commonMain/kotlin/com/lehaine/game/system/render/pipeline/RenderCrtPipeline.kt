package com.lehaine.game.system.render.pipeline

import com.lehaine.game.system.render.RenderPipeline
import com.lehaine.littlekt.extras.shader.CrtShader
import com.littlekt.Context
import com.littlekt.graphics.g2d.Batch
import com.littlekt.graphics.g2d.TextureSlice
import com.littlekt.graphics.webgpu.CommandEncoder
import com.littlekt.util.viewport.ScreenViewport

/**
 * @author Colton Daily
 * @date 2/17/2025
 */
class RenderCrtPipeline(context: Context) : RenderPipeline(context, emptyList(), "Render Crt Pipeline") {

    private val viewport = ScreenViewport(context.graphics.width, context.graphics.height)
    private val crtShader =
        CrtShader(context.graphics.device, context.graphics.preferredFormat, 4, vignette = 1f, scanlineAlpha = 0.3f)

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        crtShader.updateScanlineProperties(width.toFloat(), height.toFloat())
        viewport.update(width, height, true)
    }

    override fun render(
        batch: Batch,
        commandEncoder: CommandEncoder,
        nextRenderTargetSlice: TextureSlice?
    ): TextureSlice {
        check(nextRenderTargetSlice != null) { "Render Crt pipeline requires nextRenderTargetSlice!" }
        batch.shader = crtShader
        viewport.apply()
        val sceneRenderPass = commandEncoder.beginRenderPass(renderTargetPassDescriptor)
        sceneRenderPass.setViewport(viewport.x, viewport.y, viewport.width, viewport.height)
        batch.viewProjection = viewport.camera.viewProjection
        batch.draw(
            nextRenderTargetSlice,
            0f,
            0f,
        )
        batch.flush(
            sceneRenderPass
        )
        sceneRenderPass.end()
        sceneRenderPass.release()
        return renderTargetSlice
    }
}