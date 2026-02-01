package com.lehaine.game.system

import com.github.quillraven.fleks.IntervalSystem
import com.lehaine.game.system.render.RenderPipeline
import com.littlekt.Context
import com.littlekt.Graphics
import com.littlekt.graphics.Color
import com.littlekt.graphics.g2d.Batch
import com.littlekt.graphics.g2d.TextureSlice
import com.littlekt.graphics.g2d.use
import com.littlekt.graphics.webgpu.*
import com.littlekt.log.Logger
import com.littlekt.util.datastructure.fastForEach
import com.littlekt.util.viewport.ScreenViewport

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
class RenderSystem(
    private val batch: Batch,
    private val context: Context,
    private val pipelines: List<RenderPipeline>
) : IntervalSystem() {

    private val viewport = ScreenViewport(context.graphics.width, context.graphics.height)
    private val graphics: Graphics = context.graphics
    private val device = graphics.device

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
                    clearColor = Color.BLACK
                )
            )
        )
        var nextRenderTargetSlice: TextureSlice? = null

        batch.use {
            pipelines.fastForEach { nextRenderTargetSlice = it.render(batch, commandEncoder, nextRenderTargetSlice) }
            nextRenderTargetSlice?.let { nextRenderTargetSlice ->
                viewport.update(context.graphics.width, context.graphics.height, true)

                val renderPassEncoder = commandEncoder.beginRenderPass(frameDescriptor)
                batch.useDefaultShader()
                renderPassEncoder.setViewport(viewport.x, viewport.y, viewport.width, viewport.height)
                batch.viewProjection = viewport.camera.viewProjection
                batch.draw(nextRenderTargetSlice, 0f, 0f)
                batch.flush(renderPassEncoder)
                renderPassEncoder.end()
                renderPassEncoder.release()
            }
        }

        val commandBuffer = commandEncoder.finish()

        device.queue.submit(commandBuffer)
        graphics.surface.present()

        commandBuffer.release()
        commandEncoder.release()
        frame.release()
        swapChainTexture.release()
    }

    override fun onDispose() {
        pipelines.fastForEach { it.release() }
    }
    companion object {
        private val logger = Logger<RenderSystem>()
    }
}