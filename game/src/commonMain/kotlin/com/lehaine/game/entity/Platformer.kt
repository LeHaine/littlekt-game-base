package com.lehaine.game.entity

import com.github.quillraven.fleks.SystemConfiguration
import com.lehaine.littlekt.extras.ecs.system.PlatformerGravitySystem
import com.lehaine.littlekt.extras.ecs.system.PlatformerGroundSystem

fun SystemConfiguration.addPlatformerSystems() {
    add(PlatformerGroundSystem())
    add(PlatformerGravitySystem())
}
