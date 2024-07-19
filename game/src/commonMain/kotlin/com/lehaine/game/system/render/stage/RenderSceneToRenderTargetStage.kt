package com.lehaine.game.system.render.stage

import com.lehaine.game.system.render.RenderPipeline
import com.lehaine.game.system.render.RenderStage
import com.littlekt.graphics.Color
import com.littlekt.graphics.Texture
import com.littlekt.graphics.g2d.Batch
import com.littlekt.graphics.g2d.use
import com.littlekt.graphics.webgpu.*
import com.littlekt.util.datastructure.fastForEach
import com.littlekt.util.viewport.Viewport

/**
 * @author Colton Daily
 * @date 3/15/2023
 */
class RenderSceneToRenderTargetStage(
    private val batch: Batch,
    var renderTarget: Texture,
    private val sceneViewport: Viewport,
    override val stages: List<RenderStage>
) : RenderPipeline {

    override fun render(commandEncoder: CommandEncoder, renderPassDescriptor: RenderPassDescriptor) {
        batch.useDefaultShader()
        sceneViewport.apply()
        val renderTargetPassDescriptor = RenderPassDescriptor(
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
        val renderTargetRenderPass =
            commandEncoder.beginRenderPass(
                renderTargetPassDescriptor
            )
        batch.use(renderTargetRenderPass, sceneViewport.camera.viewProjection) {
            stages.fastForEach { it.render(commandEncoder, renderTargetPassDescriptor) }
        }
        renderTargetRenderPass.end()
        renderTargetRenderPass.release()
    }
}