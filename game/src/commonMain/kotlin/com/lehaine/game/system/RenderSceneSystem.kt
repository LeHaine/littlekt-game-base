package com.lehaine.game.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import com.lehaine.littlekt.extras.ecs.component.GridComponent
import com.lehaine.littlekt.extras.ecs.component.RenderBoundsComponent
import com.lehaine.littlekt.extras.ecs.component.SpriteComponent
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.toFloatBits
import com.lehaine.littlekt.math.Rect

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
class RenderSceneSystem(
    private val batch: Batch,
    private val viewBounds: Rect
) : IteratingSystem(World.family { all(GridComponent, SpriteComponent) }) {

    override fun onTickEntity(entity: Entity) {
        val sprite = entity[SpriteComponent]
        val grid = entity[GridComponent]
        val renderBounds = entity.getOrNull(RenderBoundsComponent)

        val slice = sprite.slice

        if (slice != null) {
            if (renderBounds == null || viewBounds.intersects(renderBounds.bounds))
                batch.draw(
                    slice,
                    grid.x,
                    grid.y,
                    grid.anchorX * slice.originalWidth,
                    grid.anchorY * slice.originalHeight,
                    width = sprite.renderWidth,
                    height = sprite.renderHeight,
                    scaleX = grid.scaleX,
                    scaleY = grid.scaleY,
                    flipX = sprite.flipX,
                    flipY = sprite.flipY,
                    rotation = grid.rotation,
                    colorBits = sprite.color.toFloatBits()
                )
        }
    }
}