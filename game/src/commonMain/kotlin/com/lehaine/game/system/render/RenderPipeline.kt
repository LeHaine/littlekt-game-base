package com.lehaine.game.system.render

import com.littlekt.Context
import com.littlekt.Releasable
import com.littlekt.graphics.Color
import com.littlekt.graphics.EmptyTexture
import com.littlekt.graphics.g2d.Batch
import com.littlekt.graphics.g2d.TextureSlice
import com.littlekt.graphics.slice
import com.littlekt.graphics.webgpu.*

abstract class RenderPipeline(protected val context: Context, val stages: List<RenderStage>, val name: String) :
    Releasable {

    val renderTarget: EmptyTexture = EmptyTexture(
        context.graphics.device,
        context.graphics.preferredFormat,
        context.graphics.width,
        context.graphics.height
    )

    var renderTargetPassDescriptor = RenderPassDescriptor(
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

    open var renderTargetSlice: TextureSlice = renderTarget.slice()

    open fun resize(width: Int, height: Int) {
        renderTarget.resize(width, height)
        renderTargetSlice = renderTarget.slice()
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

    open fun render(
        batch: Batch,
        commandEncoder: CommandEncoder,
        nextRenderTargetSlice: TextureSlice?
    ): TextureSlice {
        return renderTargetSlice
    }

    override fun release() {
        renderTarget.release()
        stages.forEach { it.release() }
    }
}