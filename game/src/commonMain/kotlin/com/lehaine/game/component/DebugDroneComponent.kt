package com.lehaine.game.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * @author Colton Daily
 * @date 3/11/2023
 */
class DebugDroneComponent : Component<DebugDroneComponent> {
    var speed = 0.12f
    var xMoveStrength = 0f
    var yMoveStrength = 0f

    override fun type() = DebugDroneComponent

    companion object : ComponentType<DebugDroneComponent>()
}