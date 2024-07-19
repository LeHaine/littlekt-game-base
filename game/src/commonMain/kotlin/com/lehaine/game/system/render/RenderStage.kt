package com.lehaine.game.system.render

import com.littlekt.graphics.webgpu.CommandEncoder
import com.littlekt.graphics.webgpu.RenderPassDescriptor

/**
 * @author Colton Daily
 * @date 3/15/2023
 */
interface RenderStage {
    fun render(commandEncoder: CommandEncoder, renderPassDescriptor: RenderPassDescriptor)
}