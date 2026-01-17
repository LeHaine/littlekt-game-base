package com.lehaine.game.system.render.stage

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityComponentContext
import com.github.quillraven.fleks.UniqueId
import com.github.quillraven.fleks.World
import com.lehaine.game.system.render.RenderIteratingStage
import com.lehaine.littlekt.extras.ecs.component.Particles
import com.littlekt.graphics.g2d.Batch
import com.littlekt.math.Rect
import com.littlekt.util.datastructure.fastForEach

/**
 * @author Colton Daily
 * @date 3/15/2023
 */
class RenderParticlesStage(
    private val viewBounds: Rect,
    vararg extraTypes: UniqueId<*> = emptyArray()
) : RenderIteratingStage(World.family { all(Particles, *extraTypes) }) {

    override fun EntityComponentContext.onRenderEntity(entity: Entity, batch: Batch) {
        val particlesComponent = entity[Particles]

        with(particlesComponent) {
            if (particles.isNotEmpty()) {
                batch.setBlendState(blendMode)
            }
            particles.fastForEach {
                if (!it.visible || it.killed) return@fastForEach

                if (viewBounds.intersects(
                        it.x + x,
                        it.y + y,
                        it.x + x + it.slice.width * it.scaleX * scaleX,
                        it.y + y + it.slice.height * it.scaleY * scaleY
                    )
                ) {
                    batch.draw(
                        it.slice,
                        it.x + x,
                        it.y + y,
                        it.anchorX * it.slice.width,
                        it.anchorY * it.slice.height,
                        scaleX = it.scaleX * scaleX,
                        scaleY = it.scaleY * scaleY,
                        rotation = it.rotation + rotation,
                        color = it.color
                    )
                }
            }

            if (particles.isNotEmpty()) {
                batch.swapToPreviousBlendState()
            }
        }
    }
}