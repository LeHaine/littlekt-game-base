package com.lehaine.game.system.render.stage

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityComponentContext
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.collection.compareEntity
import com.lehaine.game.component.DebugSprite
import com.lehaine.game.event.GameEvent
import com.lehaine.game.system.render.RenderIteratingStage
import com.lehaine.littlekt.extras.ecs.component.Grid
import com.lehaine.littlekt.extras.ecs.component.RenderBounds
import com.lehaine.littlekt.extras.ecs.component.Sprite
import com.lehaine.littlekt.extras.ecs.event.EventBus
import com.littlekt.graphics.g2d.Batch
import com.littlekt.graphics.webgpu.CommandEncoder
import com.littlekt.graphics.webgpu.RenderPassDescriptor
import com.littlekt.math.Rect

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
class DebugRenderSpritesStage(
    private val viewBounds: Rect,
    eventBus: EventBus
) : RenderIteratingStage(family = family {
    all(Grid, Sprite, DebugSprite)
}, comparator = compareEntity { entA, entB -> entA[Grid].bottom.compareTo(entB[Grid].bottom) }) {

    private var debug = false

    init {
        eventBus.register<GameEvent.ToggleDebug> { debug = !debug }
    }

    override fun render(batch: Batch, commandEncoder: CommandEncoder, renderPassDescriptor: RenderPassDescriptor) {
        if (debug) {
            super.render(batch, commandEncoder, renderPassDescriptor)
        }
    }

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
                    scaleX = grid.scaleX,
                    scaleY = grid.scaleY,
                    flipX = sprite.flipX,
                    flipY = sprite.flipY,
                    rotation = grid.rotation,
                    color = sprite.color
                )
            }
        }
    }
}