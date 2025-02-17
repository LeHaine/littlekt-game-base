package com.lehaine.game.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.game.Config
import com.lehaine.game.component.DebugDrone
import com.lehaine.littlekt.extras.ecs.component.Move

/**
 * @author Colton Daily
 * @date 3/11/2023
 */
class DebugDroneMoveSystem :
    IteratingSystem(
        family = family { all(Move, DebugDrone) },
        interval = Fixed(Config.FIXED_STEP_INTERVAL)
    ) {

    override fun onTickEntity(entity: Entity) {
        val drone = entity[DebugDrone]
        val move = entity[Move]

        move.velocityX += drone.speed * drone.xMoveStrength
        move.velocityY += drone.speed * drone.yMoveStrength
    }
}
