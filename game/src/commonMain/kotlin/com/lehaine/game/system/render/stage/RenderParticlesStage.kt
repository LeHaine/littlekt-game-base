package com.lehaine.game.system.render.stage

import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityComponentContext
import com.github.quillraven.fleks.World
import com.lehaine.game.system.render.RenderIteratingStage
import com.lehaine.littlekt.extras.ecs.component.ParticlesComponent
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.toFloatBits
import com.lehaine.littlekt.math.Rect
import com.lehaine.littlekt.util.fastForEach

/**
 * @author Colton Daily
 * @date 3/15/2023
 */
class RenderParticlesStage(
    private val batch: Batch,
    private val viewBounds: Rect,
    vararg extraTypes: ComponentType<*> = emptyArray()
) : RenderIteratingStage(World.family { all(ParticlesComponent, *extraTypes) }) {

    override fun EntityComponentContext.onRenderEntity(entity: Entity) {
        val particlesComponent = entity[ParticlesComponent]

        with(particlesComponent) {
            if (particles.isNotEmpty()) {
                batch.setBlendFunction(blendMode)
            }
            particles.fastForEach {
                if (!it.visible || !it.alive) return@fastForEach

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
                        colorBits = it.color.toFloatBits()
                    )
                }
            }

            if (particles.isNotEmpty()) {
                batch.setToPreviousBlendFunction()
            }
        }
    }
}