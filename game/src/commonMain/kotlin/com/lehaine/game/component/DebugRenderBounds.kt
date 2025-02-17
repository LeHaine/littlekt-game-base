package com.lehaine.game.component

import com.github.quillraven.fleks.ComponentType
import com.lehaine.littlekt.extras.ecs.component.PoolType
import com.lehaine.littlekt.extras.ecs.component.PoolableComponent
import com.littlekt.graphics.Color

/**
 * @author Colton Daily
 * @date 3/15/2023
 */
class DebugRenderBounds(
    var color: Color = Color.YELLOW,
    override val poolType: PoolType<DebugRenderBounds> = DebugRenderBounds
) : PoolableComponent<DebugRenderBounds> {
    override fun type() = DebugRenderBounds

    override fun reset() {
        color = Color.YELLOW
    }

    companion object : ComponentType<DebugRenderBounds>(), PoolType<DebugRenderBounds> {
        override val poolName: String = "debugRenderBoundsPool"
    }
}