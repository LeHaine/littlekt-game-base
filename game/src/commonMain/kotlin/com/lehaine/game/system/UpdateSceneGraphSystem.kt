package com.lehaine.game.system

import com.github.quillraven.fleks.IntervalSystem
import com.littlekt.graph.SceneGraph
import com.littlekt.util.seconds

/**
 * @author Colton Daily
 * @date 7/19/2024
 */
class UpdateSceneGraphSystem(private val graph: SceneGraph<*>) : IntervalSystem() {
    override fun onTick() {
        graph.update(deltaTime.seconds)
    }
}