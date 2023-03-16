package com.lehaine.game.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.game.component.DebugDroneComponent
import com.lehaine.littlekt.extras.ecs.component.MoveComponent
import com.lehaine.littlekt.input.Input
import com.lehaine.littlekt.input.Key

/**
 * @author Colton Daily
 * @date 3/11/2023
 */
class DebugDroneInputSystem(private val input: Input) :
    IteratingSystem(family { all(DebugDroneComponent, MoveComponent) }) {

    override fun onTickEntity(entity: Entity) {
        val drone = entity[DebugDroneComponent]

        drone.xMoveStrength = 0f
        drone.yMoveStrength = 0f

        if (input.isKeyPressed(Key.W)) {
            drone.yMoveStrength = -1f
        }
        if (input.isKeyPressed(Key.S)) {
            drone.yMoveStrength = 1f
        }
        if (input.isKeyPressed(Key.A)) {
            drone.xMoveStrength = -1f
        }
        if (input.isKeyPressed(Key.D)) {
            drone.xMoveStrength = 1f
        }
    }
}