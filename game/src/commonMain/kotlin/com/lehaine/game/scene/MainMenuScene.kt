package com.lehaine.game.scene

import com.lehaine.game.Assets
import com.lehaine.game.GameCore
import com.lehaine.littlekt.extras.FixedScene
import com.littlekt.Context
import com.littlekt.graph.node.ui.button
import com.littlekt.graph.node.ui.centerContainer
import com.littlekt.graph.node.ui.column
import com.littlekt.graph.node.ui.label
import com.littlekt.graph.sceneGraph
import com.littlekt.graphics.Color
import com.littlekt.graphics.HAlign
import com.littlekt.graphics.g2d.Batch
import com.littlekt.graphics.g2d.shape.ShapeRenderer
import com.littlekt.graphics.webgpu.*
import com.littlekt.util.viewport.ExtendViewport
import kotlin.time.Duration

/**
 * @author Colton Daily
 * @date 3/2/2023
 */
class MainMenuScene(context: Context, val batch: Batch, val shapeRenderer: ShapeRenderer, val core: GameCore) :
    FixedScene(context) {

    private var launching = false
    private var initialized = false

    private val graph =
        sceneGraph(
            context, ExtendViewport(960, 480), batch,
            whitePixel = Assets.atlas.getByPrefix("fxPixel").slice
        ) {
            centerContainer {
                anchorRight = 1f
                anchorTop = 1f
                column {
                    separation = 10

                    label {
                        text = "Main Menu"
                        font = Assets.pixelFont
                        horizontalAlign = HAlign.CENTER
                        fontScaleX = 2f
                        fontScaleY = 2f
                    }

                    button {
                        text = "Start"

                        onPressed += {
                            if (!launching) {
                                launching = true
                                if (!core.containsScene<GameScene>()) {
                                    core.addScene(GameScene(context, batch, shapeRenderer))
                                }
                                core.setScene<GameScene>()
                            }
                        }
                    }
                    button {
                        text = "Settings"
                        // TODO go to settings
                    }

                    if (context.platform == Context.Platform.DESKTOP) {
                        button {
                            text = "Exit"
                            onPressed += {
                                context.close()
                            }
                        }
                    }
                }
            }
        }

    override suspend fun Context.show() {
        if (!initialized) {
            initialized = true
            graph.initialize()
        } else {
            graph.controller.addInputMapProcessor(graph)
            context.input.addInputProcessor(graph)
        }
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
                label = "Main Menu Scene Pass"
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

    override suspend fun Context.hide() {
        graph.controller.removeInputMapProcessor(graph)
        context.input.removeInputProcessor(graph)
        launching = false
    }

    override fun Context.release() {
        graph.release()
    }
}