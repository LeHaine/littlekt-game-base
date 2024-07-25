package com.lehaine.game.system.hero

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.game.Config
import com.lehaine.game.component.HeroComponent
import com.lehaine.game.component.HeroCooldowns
import com.lehaine.littlekt.extras.Cooldown
import com.lehaine.littlekt.extras.ecs.component.CooldownComponent
import com.lehaine.littlekt.extras.ecs.component.GridComponent
import com.lehaine.littlekt.extras.ecs.component.MoveComponent
import com.lehaine.littlekt.extras.ecs.component.PlatformerComponent
import kotlin.time.Duration.Companion.milliseconds

/**
 * @author Colton Daily
 * @date 7/23/2024
 */
class HeroMoveSystem :
    IteratingSystem(
        family = family { all(MoveComponent, GridComponent, HeroComponent, PlatformerComponent) },
        interval = Fixed(Config.FIXED_STEP_INTERVAL)
    ) {

    override fun onTickEntity(entity: Entity) {
        val hero = entity[HeroComponent]
        val move = entity[MoveComponent]
        val cd = entity[CooldownComponent].cd
        val platformer = entity[PlatformerComponent]
        val grid = entity[GridComponent]

        move.velocityX += hero.speed * hero.xMoveStrength

        if (platformer.onGround) {
            cd.timeout(HeroCooldowns.ON_GROUND_RECENTLY, 150.milliseconds)
            cd.remove(HeroCooldowns.JUMP_FORCE)
            cd.remove(HeroCooldowns.JUMP_EXTRA)
        }

        handleJump(cd, move, grid, hero)
    }

    private fun handleJump(
        cd: Cooldown,
        move: MoveComponent,
        grid: GridComponent,
        hero: HeroComponent
    ) {
        val hasJumpForce = cd.has(HeroCooldowns.JUMP_FORCE)
        val hasJumpExtra = cd.has(HeroCooldowns.JUMP_EXTRA)
        if (hero.justJumped) {
            move.velocityY = 0.425f
            cd.timeout(HeroCooldowns.JUMP_EXTRA, 100.milliseconds)
            cd.timeout(HeroCooldowns.JUMP_FORCE, 100.milliseconds)
            cd.remove(HeroCooldowns.ON_GROUND_RECENTLY)
        } else if (hero.jumping && hasJumpExtra) {
            move.velocityY += 0.16f
        }
        if (hero.jumping && hasJumpForce) {
            move.velocityY += 0.2f * cd.ratio(HeroCooldowns.JUMP_FORCE)
        }
        hero.jumping = false
        hero.justJumped = false
    }
}
