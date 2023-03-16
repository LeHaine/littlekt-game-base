package com.lehaine.game.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.game.component.DebugDroneComponent
import com.lehaine.littlekt.extras.ecs.component.MoveComponent

/**
 * @author Colton Daily
 * @date 3/11/2023
 */
class DebugDroneMoveSystem :
    IteratingSystem(family = family { all(MoveComponent, DebugDroneComponent) }, interval = Fixed(1 / 30f)) {

    override fun onTickEntity(entity: Entity) {
        val drone = entity[DebugDroneComponent]
        val move = entity[MoveComponent]

        move.velocityX += drone.speed * drone.xMoveStrength
        move.velocityY += drone.speed * drone.yMoveStrength
    }
}