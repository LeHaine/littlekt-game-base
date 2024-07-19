package com.lehaine.game.system

import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.collection.bag
import com.lehaine.game.system.render.RenderStage
import com.littlekt.Context
import com.littlekt.Graphics
import com.littlekt.graphics.Color
import com.littlekt.graphics.webgpu.*
import com.littlekt.log.Logger
import com.littlekt.math.Rect
import com.littlekt.util.calculateViewBounds
import com.littlekt.util.datastructure.fastForEach
import com.littlekt.util.viewport.Viewport

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
class RenderSystem(
    private val context: Context,
    private val viewport: Viewport,
    private val viewBounds: Rect,
    private val stages: List<RenderStage>
) : IntervalSystem() {

    private val graphics: Graphics = context.graphics
    private val device = graphics.device
    private val preferredFormat = graphics.preferredFormat

    override fun onTick() {
        val surfaceTexture = graphics.surface.getCurrentTexture()
        when (val status = surfaceTexture.status) {
            TextureStatus.SUCCESS -> {
                // all good, could check for `surfaceTexture.suboptimal` here.
            }

            TextureStatus.TIMEOUT,
            TextureStatus.OUTDATED,
            TextureStatus.LOST -> {
                surfaceTexture.texture?.release()
                logger.info { "getCurrentTexture status=$status" }
                return
            }

            else -> {
                // fatal
                logger.fatal { "getCurrentTexture status=$status" }
                context.close()
                return
            }
        }
        val swapChainTexture = checkNotNull(surfaceTexture.texture)
        val frame = swapChainTexture.createView()

        val commandEncoder = device.createCommandEncoder()
        val frameDescriptor = RenderPassDescriptor(
            listOf(
                RenderPassColorAttachmentDescriptor(
                    view = frame,
                    loadOp = LoadOp.CLEAR,
                    storeOp = StoreOp.STORE,
                    clearColor =
                    if (preferredFormat.srgb) Color.DARK_GRAY.toLinear()
                    else Color.DARK_GRAY
                )
            )
        )
        viewport.apply()
        viewBounds.calculateViewBounds(viewport.camera)

        stages.fastForEach {
            it.render(commandEncoder, frameDescriptor)
        }

        val commandBuffer = commandEncoder.finish()

        device.queue.submit(commandBuffer)
        graphics.surface.present()

        commandBuffer.release()
        commandEncoder.release()
        frame.release()
        swapChainTexture.release()
    }

    companion object {
        private val logger = Logger<RenderSystem>()
    }
}