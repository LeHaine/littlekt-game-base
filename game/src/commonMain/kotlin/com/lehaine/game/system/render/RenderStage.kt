package com.lehaine.game.system.render

import com.littlekt.Releasable
import com.littlekt.graphics.g2d.Batch
import com.littlekt.graphics.webgpu.CommandEncoder
import com.littlekt.graphics.webgpu.RenderPassDescriptor
import com.littlekt.graphics.webgpu.RenderPassEncoder
import com.littlekt.math.Mat4

/**
 * @author Colton Daily
 * @date 3/15/2023
 */
interface RenderStage : Releasable {

    interface GroupedStage : RenderStage {
        val stages: List<RenderStage>
    }

    interface BatchStage : RenderStage {
        fun render(batch: Batch, commandEncoder: CommandEncoder, renderPassDescriptor: RenderPassDescriptor)
    }

    interface CacheStage : RenderStage {
        fun render(renderPassEncoder: RenderPassEncoder, viewProjection: Mat4)
    }

    override fun release() = Unit
}