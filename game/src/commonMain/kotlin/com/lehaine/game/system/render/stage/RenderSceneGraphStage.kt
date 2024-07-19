package com.lehaine.game.system.render.stage

import com.lehaine.game.system.render.RenderStage
import com.littlekt.graph.SceneGraph
import com.littlekt.graphics.g2d.Batch
import com.littlekt.graphics.webgpu.CommandEncoder
import com.littlekt.graphics.webgpu.LoadOp
import com.littlekt.graphics.webgpu.RenderPassDescriptor

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
class RenderSceneGraphStage(private val batch: Batch, private val graph: SceneGraph<*>) : RenderStage {

    override fun render(commandEncoder: CommandEncoder, renderPassDescriptor: RenderPassDescriptor) {
        batch.useDefaultShader()
        val uiRenderDesc = run {
            val colorAttachments = renderPassDescriptor.colorAttachments.map { it.copy(loadOp = LoadOp.LOAD) }
            renderPassDescriptor.copy(colorAttachments = colorAttachments, label = "Render Scene Graph Stage")
        }
        graph.render(commandEncoder, uiRenderDesc)
        if (batch.drawing) batch.end()
    }
}