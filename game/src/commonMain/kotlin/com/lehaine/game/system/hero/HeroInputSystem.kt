package com.lehaine.game.system.hero

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.game.ControllerOwner
import com.lehaine.game.GameInput
import com.lehaine.game.component.Hero
import com.lehaine.game.component.HeroCooldowns
import com.lehaine.game.event.GameEvent
import com.lehaine.littlekt.extras.ecs.component.CooldownComponent
import com.lehaine.littlekt.extras.ecs.event.EventBus
import com.littlekt.input.InputMapController

/**
 * @author Colton Daily
 * @date 7/23/2024
 */
class HeroInputSystem(private val controller: InputMapController<GameInput>, eventBus: EventBus) :
    IteratingSystem(family { all(Hero, CooldownComponent) }) {
    private var ownsController = false

    init {
        eventBus.register<GameEvent.LockController> {
            ownsController = it.owner == ControllerOwner.PLAYER
        }
    }

    override fun onTickEntity(entity: Entity) {
        val cd = entity[CooldownComponent].cd
        if (ownsController && !cd.has(HeroCooldowns.STUNNED)) {
            val movement = controller.vector(GameInput.MOVEMENT)
            val hero = entity[Hero]

            hero.xMoveStrength = movement.x
            hero.yMoveStrength = movement.y

            val jumpPressed = controller.pressed(GameInput.JUMP)
            val jumpDown = controller.down(GameInput.JUMP)

            if (jumpPressed && cd.has(HeroCooldowns.ON_GROUND_RECENTLY)) {
                hero.justJumped = true
            } else if (
                jumpDown && (cd.has(HeroCooldowns.JUMP_EXTRA) || cd.has(HeroCooldowns.JUMP_FORCE))
            ) {
                hero.jumping = true
            }
        }
    }
}
