package com.lehaine.game.scene

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.extras.FixedScene
import com.lehaine.littlekt.graph.node.ui.centerContainer
import com.lehaine.littlekt.graph.node.ui.label
import com.lehaine.littlekt.graph.sceneGraph
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.gl.ClearBufferMask
import com.lehaine.littlekt.util.viewport.ExtendViewport
import kotlin.time.Duration

/**
 * @author Colton Daily
 * @date 3/1/2023
 */
class LoadingScene(context: Context, val batch: Batch) : FixedScene(context) {

    private val graph = sceneGraph(context, ExtendViewport(960, 480), batch) {
        centerContainer {
            anchorRight = 1f
            anchorBottom = 1f
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

    override fun Context.render(dt: Duration) {
        gl.clearColor(0.1f, 0.1f, 0.1f, 1f)
        gl.clear(ClearBufferMask.COLOR_BUFFER_BIT)

        graph.update(dt)
        graph.render()
    }

    override fun Context.dispose() {
        graph.dispose()
    }

}