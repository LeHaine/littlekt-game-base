package com.lehaine.game.system.hero

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.game.component.Hero
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
            all(Move, Grid, CooldownComponent, Hero, Platformer)
        }
    ) {

    override fun onTickEntity(entity: Entity) {
        val hero = entity[Hero]
        val move = entity[Move]
        val cd = entity[CooldownComponent].cd
        val platformer = entity[Platformer]
        val grid = entity[Grid]

        val collisionY = entity.getOrNull(GridCollisionResult.GridCollisionY)
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

    private fun handleLanding(cd: Cooldown, grid: Grid, hero: Hero) {
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
