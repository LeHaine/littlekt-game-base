package com.lehaine.game.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.lehaine.littlekt.graphics.Color

/**
 * @author Colton Daily
 * @date 3/15/2023
 */
class DebugRenderBoundsComponent(var color: Color = Color.YELLOW) : Component<DebugRenderBoundsComponent> {
    override fun type() = DebugRenderBoundsComponent

    companion object : ComponentType<DebugRenderBoundsComponent>()
}