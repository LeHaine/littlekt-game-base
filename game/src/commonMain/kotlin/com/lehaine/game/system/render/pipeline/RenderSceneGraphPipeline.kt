package com.lehaine.game.system.render.pipeline

import com.lehaine.game.system.render.RenderPipeline
import com.lehaine.game.system.render.RenderStage
import com.littlekt.Context
import com.littlekt.graph.SceneGraph
import com.littlekt.graphics.g2d.Batch
import com.littlekt.graphics.g2d.TextureSlice
import com.littlekt.graphics.webgpu.CommandEncoder
import com.littlekt.graphics.webgpu.LoadOp
import com.littlekt.graphics.webgpu.RenderPassDescriptor
import com.littlekt.util.datastructure.fastForEach

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
class RenderSceneGraphPipeline(
    context: Context,
    private val graph: SceneGraph<*>,
    stages: List<RenderStage> = emptyList()
) : RenderPipeline(context, stages, "Render SceneGraph Pipeline") {

    override fun render(
        batch: Batch,
        commandEncoder: CommandEncoder,
        nextRenderTargetSlice: TextureSlice?
    ): TextureSlice {
        batch.useDefaultShader()
        nextRenderTargetSlice?.let { batch.draw(it, 0f, 0f) }
        stages.fastForEach { stage ->
            when (stage) {
                is RenderStage.BatchStage -> stage.render(batch, commandEncoder, renderTargetPassDescriptor)
            }
        }
        graph.render(commandEncoder, renderTargetPassDescriptor)

        return renderTargetSlice
    }
}