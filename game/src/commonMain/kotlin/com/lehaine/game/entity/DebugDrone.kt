package com.lehaine.game.entity

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.SystemConfiguration
import com.github.quillraven.fleks.World
import com.lehaine.game.Config
import com.lehaine.game.GameInput
import com.lehaine.game.component.DebugDroneComponent
import com.lehaine.game.component.DebugSpriteComponent
import com.lehaine.game.system.DebugDroneInputSystem
import com.lehaine.game.system.DebugDroneMoveSystem
import com.lehaine.littlekt.extras.ecs.component.GridComponent
import com.lehaine.littlekt.extras.ecs.component.MoveComponent
import com.lehaine.littlekt.extras.ecs.component.SpriteComponent
import com.lehaine.littlekt.extras.ecs.event.EventBus
import com.littlekt.graphics.Color
import com.littlekt.graphics.g2d.TextureSlice
import com.littlekt.input.Input
import com.littlekt.input.InputMapController

fun SystemConfiguration.addDroneSystems(controller: InputMapController<GameInput>, eventBus: EventBus) {
    add(DebugDroneInputSystem(controller, eventBus))
    add(DebugDroneMoveSystem())
}

fun World.debugDrone(slice: TextureSlice): Entity = entity {
    it += DebugDroneComponent()
    it += MoveComponent()
    it += SpriteComponent(slice).apply { color.set(Color.YELLOW) }
    it += GridComponent(Config.GRID_CELL_SIZE_F).apply {
        scaleX = 10f
        scaleY = 10f
    }
    it += DebugSpriteComponent()
}

