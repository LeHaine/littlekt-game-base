package com.lehaine.game.system.render

import com.littlekt.Releasable
import com.littlekt.graphics.g2d.Batch
import com.littlekt.graphics.webgpu.CommandEncoder
import com.littlekt.graphics.webgpu.RenderPassDescriptor

/**
 * @author Colton Daily
 * @date 3/15/2023
 */
interface RenderStage : Releasable {
    fun render(batch: Batch, commandEncoder: CommandEncoder, renderPassDescriptor: RenderPassDescriptor)
    override fun release() = Unit
}