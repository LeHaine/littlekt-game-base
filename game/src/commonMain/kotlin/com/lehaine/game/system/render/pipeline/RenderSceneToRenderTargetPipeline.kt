package com.lehaine.game.system.render.pipeline

import com.lehaine.game.Config
import com.lehaine.game.system.render.RenderPipeline
import com.lehaine.game.system.render.RenderStage
import com.lehaine.littlekt.extras.graphics.PixelSmoothRenderTarget
import com.littlekt.Context
import com.littlekt.graphics.Color
import com.littlekt.graphics.g2d.Batch
import com.littlekt.graphics.g2d.TextureSlice
import com.littlekt.graphics.webgpu.*
import com.littlekt.math.Rect
import com.littlekt.util.calculateViewBounds
import com.littlekt.util.datastructure.fastForEach
import com.littlekt.util.viewport.ScreenViewport
import com.littlekt.util.viewport.Viewport

/**
 * @author Colton Daily
 * @date 3/15/2023
 */
class RenderSceneToRenderTargetPipeline(
    context: Context,
    sceneViewport: ScreenViewport,
    private val sceneCameraViewBounds: Rect,
    stages: List<RenderStage>,
) : RenderPipeline(context, stages, "Render Scene to Render Target Pipeline") {
    private val sceneViewport: Viewport = sceneViewport
    private var sceneRenderTarget: PixelSmoothRenderTarget =
        PixelSmoothRenderTarget(
            context.graphics.width,
            context.graphics.height,
            Config.TARGET_HEIGHT,
        )
    override var renderTargetSlice: TextureSlice =
        TextureSlice(
            renderTarget,
            0,
            sceneRenderTarget.height - sceneRenderTarget.pxHeight,
            sceneRenderTarget.pxWidth,
            sceneRenderTarget.pxHeight,
        )

    override fun resize(width: Int, height: Int) {
        sceneRenderTarget =
            PixelSmoothRenderTarget(
                width,
                height,
                Config.TARGET_HEIGHT,
            )
        renderTarget.resize(sceneRenderTarget.width, sceneRenderTarget.height)
        renderTargetSlice =
            TextureSlice(
                renderTarget,
                0,
                sceneRenderTarget.height - sceneRenderTarget.pxHeight,
                sceneRenderTarget.pxWidth,
                sceneRenderTarget.pxHeight,
            )
        sceneViewport.update(sceneRenderTarget.width, sceneRenderTarget.height)
        renderTargetPassDescriptor = RenderPassDescriptor(
            listOf(
                RenderPassColorAttachmentDescriptor(
                    view = renderTarget.view,
                    loadOp = LoadOp.CLEAR,
                    storeOp = StoreOp.STORE,
                    clearColor = Color.CLEAR
                )
            ),
            label = "Render Scene To Target pass"
        )
    }

    override fun render(
        batch: Batch,
        commandEncoder: CommandEncoder,
        nextRenderTargetSlice: TextureSlice?
    ): TextureSlice {
        batch.useDefaultShader()
        sceneViewport.apply()
        sceneCameraViewBounds.calculateViewBounds(sceneViewport.camera)
        val renderTargetRenderPass = commandEncoder.beginRenderPass(renderTargetPassDescriptor)
        batch.viewProjection = sceneViewport.camera.viewProjection
        nextRenderTargetSlice?.let { batch.draw(it, 0f, 0f) }
        stages.fastForEach { it.render(batch, commandEncoder, renderTargetPassDescriptor) }
        batch.flush(renderTargetRenderPass)

        renderTargetRenderPass.end()
        renderTargetRenderPass.release()
        return renderTargetSlice
    }
}