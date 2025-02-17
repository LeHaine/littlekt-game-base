package com.lehaine.game.entity

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.SystemConfiguration
import com.github.quillraven.fleks.World
import com.lehaine.game.Config
import com.lehaine.game.GameInput
import com.lehaine.game.component.DebugDrone
import com.lehaine.game.component.DebugSprite
import com.lehaine.game.system.DebugDroneInputSystem
import com.lehaine.game.system.DebugDroneMoveSystem
import com.lehaine.littlekt.extras.ecs.component.Grid
import com.lehaine.littlekt.extras.ecs.component.Move
import com.lehaine.littlekt.extras.ecs.component.Sprite
import com.lehaine.littlekt.extras.ecs.event.EventBus
import com.littlekt.graphics.Color
import com.littlekt.graphics.g2d.TextureSlice
import com.littlekt.input.InputMapController

fun SystemConfiguration.addDroneSystems(controller: InputMapController<GameInput>, eventBus: EventBus) {
    add(DebugDroneInputSystem(controller, eventBus))
    add(DebugDroneMoveSystem())
}

fun World.debugDrone(slice: TextureSlice): Entity = entity {
    it += DebugDrone()
    it += Move()
    it += Sprite(slice).apply { color.set(Color.YELLOW) }
    it += Grid(Config.GRID_CELL_SIZE_F).apply {
        scaleX = 10f
        scaleY = 10f
    }
    it += DebugSprite
}

