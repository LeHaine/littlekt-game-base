package com.lehaine.game.scene

import com.lehaine.game.Assets
import com.lehaine.game.Config
import com.lehaine.game.Fx
import com.lehaine.game.createUiGameInputSignals
import com.lehaine.littlekt.Context
import com.lehaine.littlekt.extras.FixedScene
import com.lehaine.littlekt.graph.node.ui.Control
import com.lehaine.littlekt.graph.node.ui.centerContainer
import com.lehaine.littlekt.graph.node.ui.control
import com.lehaine.littlekt.graph.node.ui.label
import com.lehaine.littlekt.graph.sceneGraph
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.graphics.gl.ClearBufferMask
import com.lehaine.littlekt.util.viewport.ExtendViewport
import kotlin.time.Duration

/**
 * @author Colton Daily
 * @date 3/1/2023
 */
class GameScene(context: Context, val batch: Batch, val shapeRenderer: ShapeRenderer) : FixedScene(context) {

    private val fx = Fx()
    private val viewport = ExtendViewport(Config.VIRTUAL_WIDTH, Config.VIRTUAL_HEIGHT)
    private val ui: Control
    private val graph =
        sceneGraph(context, ExtendViewport(960, 540), batch, uiInputSignals = createUiGameInputSignals()) {
            ui = control {
                name = "UI"
                anchorRight = 1f
                anchorBottom = 1f

                centerContainer {
                    anchorRight = 1f
                    anchorBottom = 1f
                    label {
                        text = "TODO: Implement game logic"
                        font = Assets.pixelFont
                    }
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

    override fun Context.render(dt: Duration) {
        gl.clearColor(0.1f, 0.1f, 0.1f, 1f)
        gl.clear(ClearBufferMask.COLOR_BUFFER_BIT)

        fx.update(dt)

        viewport.apply(this)

        // render background
        fx.bgNormal.render(batch, viewport.camera, shapeRenderer)
        fx.bgAdd.render(batch, viewport.camera, shapeRenderer)


        // TODO render main


        // render top
        fx.bgNormal.render(batch, viewport.camera, shapeRenderer)
        fx.bgAdd.render(batch, viewport.camera, shapeRenderer)


        // render UI
        graph.update(dt)
        graph.render()
    }

    override fun Context.dispose() {
        graph.dispose()
    }
}