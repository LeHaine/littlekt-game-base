package com.lehaine.game.scene

import com.lehaine.littlekt.extras.FixedScene
import com.littlekt.Context
import com.littlekt.graph.node.ui.centerContainer
import com.littlekt.graph.node.ui.label
import com.littlekt.graph.sceneGraph
import com.littlekt.graphics.Color
import com.littlekt.graphics.g2d.Batch
import com.littlekt.graphics.webgpu.*
import com.littlekt.util.viewport.ExtendViewport
import kotlin.time.Duration

/**
 * @author Colton Daily
 * @date 3/1/2023
 */
class LoadingScene(context: Context, val batch: Batch) : FixedScene(context) {

    private val graph = sceneGraph(context, ExtendViewport(960, 480), batch) {
        centerContainer {
            anchorRight = 1f
            anchorTop = 1f
            label {
                text = "Loading..."
            }
        }
    }

    override suspend fun Context.show() {
        graph.initialize()
        graph.resize(graphics.width, graphics.height, true)
    }

    override fun Context.resize(width: Int, height: Int) {
        graph.resize(width, height, true)
    }

    override fun Context.update(dt: Duration) {
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
                close()
                return
            }
        }
        val swapChainTexture = checkNotNull(surfaceTexture.texture)
        val frame = swapChainTexture.createView()

        val commandEncoder = graphics.device.createCommandEncoder("scenegraph command encoder")
        val renderPassDescriptor =
            RenderPassDescriptor(
                listOf(
                    RenderPassColorAttachmentDescriptor(
                        view = frame,
                        loadOp = LoadOp.CLEAR,
                        storeOp = StoreOp.STORE,
                        clearColor =
                        if (graphics.preferredFormat.srgb) Color.DARK_GRAY.toLinear()
                        else Color.DARK_GRAY
                    )
                ),
                label = "Loading Scene Pass"
            )
        graph.update(dt)
        graph.render(commandEncoder, renderPassDescriptor)
        if (batch.drawing) batch.end()

        val commandBuffer = commandEncoder.finish()

        graphics.device.queue.submit(commandBuffer)
        graphics.surface.present()

        commandBuffer.release()
        commandEncoder.release()
        frame.release()
        swapChainTexture.release()
    }

    override fun Context.release() {
        graph.release()
    }

}