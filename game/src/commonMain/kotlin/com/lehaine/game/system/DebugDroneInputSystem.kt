package com.lehaine.game.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.game.ControllerOwner
import com.lehaine.game.GameInput
import com.lehaine.game.component.DebugDroneComponent
import com.lehaine.game.event.GameEvent
import com.lehaine.littlekt.extras.ecs.component.MoveComponent
import com.lehaine.littlekt.extras.ecs.event.EventBus
import com.littlekt.input.InputMapController

/**
 * @author Colton Daily
 * @date 3/11/2023
 */
class DebugDroneInputSystem(
    private val controller: InputMapController<GameInput>,
    eventBus: EventBus
) : IteratingSystem(family { all(DebugDroneComponent, MoveComponent) }) {

    private var ownsController = false

    init {
        eventBus.register<GameEvent.LockController> {
            ownsController = it.owner == ControllerOwner.DEBUG
        }
    }

    override fun onTickEntity(entity: Entity) {
        val drone = entity[DebugDroneComponent]

        drone.xMoveStrength = 0f
        drone.yMoveStrength = 0f

        if (ownsController) {
            val movement = controller.vector(GameInput.MOVEMENT)
            drone.xMoveStrength = movement.x
            drone.yMoveStrength = movement.y
        }
    }
}
