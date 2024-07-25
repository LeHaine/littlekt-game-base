package com.lehaine.game.system.hero

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.game.component.HeroComponent
import com.lehaine.game.component.HeroCooldowns
import com.lehaine.littlekt.extras.Cooldown
import com.lehaine.littlekt.extras.ecs.component.*
import kotlin.math.max
import kotlin.math.sign
import kotlin.time.Duration.Companion.milliseconds

/**
 * @author Colton Daily
 * @date 7/24/2024
 */
class HeroSystem :
    IteratingSystem(
        family {
            all(MoveComponent, GridComponent, CooldownComponent, HeroComponent, PlatformerComponent)
        }
    ) {

    override fun onTickEntity(entity: Entity) {
        val hero = entity[HeroComponent]
        val move = entity[MoveComponent]
        val cd = entity[CooldownComponent].cd
        val platformer = entity[PlatformerComponent]
        val grid = entity[GridComponent]

        val collisionY = entity.getOrNull(GridCollisionResultComponent.GridCollisionY)
        if (collisionY != null) {
            if (collisionY.dir == -1) {
                handleLanding(cd, grid, hero)
            }
        }

        if (move.velocityY >= 0f || platformer.onGround) {
            hero.fallStartCy = grid.cy + grid.yr
        }

        if (hero.justJumped) {
            grid.squashX = 0.8f
        }

        if (move.velocityX != 0f) {
            grid.dir = move.velocityX.sign.toInt()
        }
    }

    private fun handleLanding(cd: Cooldown, grid: GridComponent, hero: HeroComponent) {
        val fallHeight = max(0f, hero.fallStartCy - grid.cy + grid.yr)
        if (fallHeight > 15f) {
            cd.timeout(HeroCooldowns.STUNNED, 400.milliseconds)
            grid.squashY = 0.6f
        } else if (fallHeight > 5f) {
            grid.squashY = 0.6f
        } else if (fallHeight > 2f) {
            grid.squashY = 0.8f
        }
    }
}
