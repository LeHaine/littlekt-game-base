package com.lehaine.game.system.render.stage

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityComponentContext
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.game.component.DebugRenderBoundsComponent
import com.lehaine.game.event.ToggleDebug
import com.lehaine.game.system.render.RenderIteratingStage
import com.lehaine.littlekt.extras.ecs.component.RenderBoundsComponent
import com.lehaine.littlekt.extras.ecs.event.EventBus
import com.littlekt.graphics.g2d.shape.ShapeRenderer
import com.littlekt.graphics.webgpu.CommandEncoder
import com.littlekt.graphics.webgpu.RenderPassDescriptor
import com.littlekt.math.Rect

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
class DebugRenderBoundsStage(
    private val shapeRenderer: ShapeRenderer,
    private val viewBounds: Rect,
    eventBus: EventBus
) : RenderIteratingStage(
    family = family {
        all(DebugRenderBoundsComponent, RenderBoundsComponent)
    }) {

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
        val renderBounds = entity[RenderBoundsComponent].bounds
        val debugColor = entity[DebugRenderBoundsComponent].color

        if (viewBounds.intersects(renderBounds)) {
            shapeRenderer.rectangle(renderBounds, color = debugColor)
        }
    }
}