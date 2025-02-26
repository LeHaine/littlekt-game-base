package com.lehaine.game.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * @author Colton Daily
 * @date 7/23/2024
 */
class Hero : Component<Hero> {
    var speed = 0.06f
    var xMoveStrength = 0f
    var yMoveStrength = 0f
    var fallStartCy = 99999f
    var jumping = false
    var justJumped = false

    override fun type() = Hero

    data object Cooldowns {
        const val JUMP_EXTRA = "jumpExtra"
        const val JUMP_FORCE = "jumpForce"
        const val ON_GROUND_RECENTLY = "onGroundRecently"
        const val STUNNED = "stunned"
    }

    companion object : ComponentType<Hero>()
}

typealias HeroCooldowns = Hero.Cooldowns
