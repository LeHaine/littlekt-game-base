package com.lehaine.game.system

import com.github.quillraven.fleks.IntervalSystem
import com.lehaine.littlekt.graphics.g2d.ParticleSimulator
import com.lehaine.littlekt.util.seconds

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
class ParticleSimulatorSystem(private val simulator: ParticleSimulator = ParticleSimulator(2048)) : IntervalSystem() {
    private var tmod = 1f
    private var targetFPS = 60

    override fun onTick() {
        tmod = deltaTime * targetFPS
        simulator.update(deltaTime.seconds, tmod)
    }
}