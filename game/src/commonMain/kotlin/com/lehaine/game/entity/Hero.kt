package com.lehaine.game.entity

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.SystemConfiguration
import com.github.quillraven.fleks.World
import com.lehaine.game.Assets
import com.lehaine.game.Config
import com.lehaine.game.GameInput
import com.lehaine.game.Level
import com.lehaine.game.component.HeroComponent
import com.lehaine.game.system.hero.HeroInputSystem
import com.lehaine.game.system.hero.HeroMoveSystem
import com.lehaine.game.system.hero.HeroSystem
import com.lehaine.littlekt.extras.ecs.component.*
import com.lehaine.littlekt.extras.ecs.event.EventBus
import com.lehaine.littlekt.extras.ecs.logic.collision.checker.LevelCollisionChecker
import com.lehaine.littlekt.extras.ecs.logic.collision.checker.LevelGroundChecker
import com.lehaine.littlekt.extras.ecs.logic.collision.resolver.PlatformerLevelCollisionResolver
import com.littlekt.graphics.g2d.tilemap.ldtk.LDtkEntity
import com.littlekt.input.InputMapController

fun SystemConfiguration.addHeroSystems(
    controller: InputMapController<GameInput>,
    eventBus: EventBus
) {
    add(HeroSystem())
    add(HeroInputSystem(controller, eventBus))
    add(HeroMoveSystem())
}

fun World.hero(data: LDtkEntity, level: Level): Entity = entity { entity ->
    val move = MoveComponent().also { entity += it }
    addSpriteBundle(entity)
    entity[SpriteComponent].slice = Assets.atlas.getByPrefix("fxPixel").slice
    entity[GridComponent].apply {
        setFromLevelEntity(data, level)
        height = gridCellSize
        // scales the sprite
        scaleX = gridCellSize * 0.5f
        scaleY = gridCellSize
    }

    entity += HeroComponent()
    entity += CooldownComponent()
    val platformer = PlatformerComponent(LevelGroundChecker(level)).also { entity += it }
    // TODO add animation component
//    entity +=
//        AnimationComponent().apply {
//            registerState(Assets.heroJumpUp, 7) { !platformer.onGround && move.velocityY > 0f }
//            registerState(Assets.heroJumpDown, 6) { !platformer.onGround }
//            registerState(Assets.heroRun, 5) {
//                !move.velocityX.isFuzzyZero(0.05f) && platformer.onGround
//            }
//            registerState(Assets.heroIdle, 0)
//        }
    entity += GravityComponent().apply { gravityY = -0.075f }
    entity += GridCollisionComponent(LevelCollisionChecker(level))
    entity += GridCollisionResolverComponent(PlatformerLevelCollisionResolver)
}

private fun GridComponent.setFromLevelEntity(data: LDtkEntity, level: Level) {
    anchorX = data.pivotX
    anchorY = 1f - data.pivotY
    toGridPosition(
        cx = data.cx,
        cy = level.height / level.gridSize - 1 - data.cy,
        xr = data.pivotX,
        yr = 1f - data.pivotY,
    )
}
