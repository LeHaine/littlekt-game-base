package com.lehaine.game.system.render.stage

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityComponentContext
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.collection.compareEntity
import com.lehaine.game.component.DebugSprite
import com.lehaine.game.system.render.RenderIteratingBatchStage
import com.lehaine.littlekt.extras.ecs.component.Grid
import com.lehaine.littlekt.extras.ecs.component.RenderBounds
import com.lehaine.littlekt.extras.ecs.component.Sprite
import com.littlekt.graphics.g2d.Batch
import com.littlekt.math.Rect

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
class RenderSpritesStage(private val viewBounds: Rect) :
    RenderIteratingBatchStage(
        family = family {
            all(Grid, Sprite).none(DebugSprite)
        },
        comparator = compareEntity { entA, entB -> entA[Grid].bottom.compareTo(entB[Grid].bottom) }) {

    override fun EntityComponentContext.onRenderEntity(entity: Entity, batch: Batch) {
        val sprite = entity[Sprite]
        val grid = entity[Grid]
        val renderBounds = entity.getOrNull(RenderBounds)

        val slice = sprite.slice

        if (slice != null) {
            if (renderBounds == null || viewBounds.intersects(renderBounds.bounds)) {
                batch.draw(
                    slice,
                    grid.x,
                    grid.y,
                    grid.anchorX * slice.actualWidth,
                    grid.anchorY * slice.actualHeight,
                    width = sprite.renderWidth,
                    height = sprite.renderHeight,
                    scaleX = grid.currentScaleX,
                    scaleY = grid.currentScaleY,
                    flipX = sprite.flipX,
                    flipY = sprite.flipY,
                    rotation = grid.rotation,
                    color = sprite.color
                )
            }
        }
    }
}