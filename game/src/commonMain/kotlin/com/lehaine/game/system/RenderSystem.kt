package com.lehaine.game.system

import com.github.quillraven.fleks.IntervalSystem
import com.lehaine.game.system.render.RenderStage
import com.lehaine.littlekt.Context
import com.lehaine.littlekt.Graphics
import com.lehaine.littlekt.graphics.GL
import com.lehaine.littlekt.graphics.gl.ClearBufferMask
import com.lehaine.littlekt.graphics.gl.State
import com.lehaine.littlekt.math.Rect
import com.lehaine.littlekt.util.calculateViewBounds
import com.lehaine.littlekt.util.fastForEach
import com.lehaine.littlekt.util.viewport.Viewport

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

    private val gl: GL get() = context.gl
    private val graphics: Graphics get() = context.graphics

    override fun onTick() {
        gl.enable(State.SCISSOR_TEST)
        gl.scissor(0, 0, graphics.width, graphics.height)
        gl.clearColor(0.1f, 0.1f, 0.1f, 1f)
        gl.clear(ClearBufferMask.COLOR_BUFFER_BIT)

        viewport.apply(context)
        viewBounds.calculateViewBounds(viewport.camera)

        stages.fastForEach {
            it.render()
        }

        gl.disable(State.SCISSOR_TEST)
    }
}