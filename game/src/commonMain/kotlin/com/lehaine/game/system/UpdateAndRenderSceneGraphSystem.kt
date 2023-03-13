package com.lehaine.game.system

import com.github.quillraven.fleks.IntervalSystem
import com.lehaine.littlekt.graph.SceneGraph
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.util.seconds

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
class UpdateAndRenderSceneGraphSystem(private val batch: Batch, private val graph: SceneGraph<*>) : IntervalSystem() {

    override fun onTick() {
        batch.useDefaultShader()
        graph.update(deltaTime.seconds)
        graph.render()
    }
}