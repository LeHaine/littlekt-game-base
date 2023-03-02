package com.lehaine.game.scene

import com.lehaine.game.Assets
import com.lehaine.game.GameCore
import com.lehaine.littlekt.Context
import com.lehaine.littlekt.extras.FixedScene
import com.lehaine.littlekt.graph.node.resource.HAlign
import com.lehaine.littlekt.graph.node.ui.button
import com.lehaine.littlekt.graph.node.ui.centerContainer
import com.lehaine.littlekt.graph.node.ui.column
import com.lehaine.littlekt.graph.node.ui.label
import com.lehaine.littlekt.graph.sceneGraph
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.graphics.gl.ClearBufferMask
import com.lehaine.littlekt.util.viewport.ExtendViewport
import kotlin.time.Duration

/**
 * @author Colton Daily
 * @date 3/2/2023
 */
class MainMenuScene(context: Context, val batch: Batch, val shapeRenderer: ShapeRenderer, val core: GameCore) :
    FixedScene(context) {

    private var launching = false
    private var initialized = false

    private val graph = sceneGraph(context, ExtendViewport(960, 480), batch) {
        centerContainer {
            anchorRight = 1f
            anchorBottom = 1f
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
                            if(!core.containsScene<GameScene>()) {
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

    override fun Context.render(dt: Duration) {
        gl.clearColor(0.1f, 0.1f, 0.1f, 1f)
        gl.clear(ClearBufferMask.COLOR_BUFFER_BIT)

        graph.update(dt)
        graph.render()
    }

    override suspend fun Context.hide() {
        graph.controller.removeInputMapProcessor(graph)
        context.input.removeInputProcessor(graph)
        launching = false
    }

    override fun Context.dispose() {
        graph.dispose()
    }
}