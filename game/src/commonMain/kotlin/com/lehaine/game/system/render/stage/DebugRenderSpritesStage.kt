package com.lehaine.game.system.render.stage

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityComponentContext
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.collection.compareEntity
import com.lehaine.game.component.DebugSpriteComponent
import com.lehaine.game.event.ToggleDebug
import com.lehaine.game.system.render.RenderIteratingStage
import com.lehaine.littlekt.extras.ecs.component.GridComponent
import com.lehaine.littlekt.extras.ecs.component.RenderBoundsComponent
import com.lehaine.littlekt.extras.ecs.component.SpriteComponent
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
    private val batch: Batch,
    private val viewBounds: Rect,
    eventBus: EventBus
) : RenderIteratingStage(family = family {
    all(GridComponent, SpriteComponent, DebugSpriteComponent)
}, comparator = compareEntity { entA, entB -> entA[GridComponent].bottom.compareTo(entB[GridComponent].bottom) }) {

    private var debug = false

    init {
        eventBus.register<ToggleDebug> { debug = !debug }
    }

    override fun render(commandEncoder: CommandEncoder, renderPassDescriptor: RenderPassDescriptor) {
        if (debug) {
            super.render(commandEncoder, renderPassDescriptor)
        }
    }

    override fun EntityComponentContext.onRenderEntity(entity: Entity) {
        val sprite = entity[SpriteComponent]
        val grid = entity[GridComponent]
        val renderBounds = entity.getOrNull(RenderBoundsComponent)

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