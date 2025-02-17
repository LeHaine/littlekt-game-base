package com.lehaine.game.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * @author Colton Daily
 * @date 3/11/2023
 */
class DebugDrone : Component<DebugDrone> {
    var speed = 0.12f
    var xMoveStrength = 0f
    var yMoveStrength = 0f

    override fun type() = DebugDrone

    companion object : ComponentType<DebugDrone>()
}