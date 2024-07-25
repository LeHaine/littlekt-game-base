package com.lehaine.game.entity

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.lehaine.game.Config
import com.lehaine.game.component.DebugRenderBoundsComponent
import com.lehaine.littlekt.extras.ecs.component.GridComponent
import com.lehaine.littlekt.extras.ecs.component.RenderBoundsComponent
import com.lehaine.littlekt.extras.ecs.component.SpriteComponent

/**
 * Adds a [SpriteComponent], [RenderBoundsComponent], and [GridComponent] to the specified [entity].
 */
fun EntityCreateContext.addSpriteBundle(entity: Entity) {
    entity += SpriteComponent()
    entity += RenderBoundsComponent()
    entity += DebugRenderBoundsComponent()
    entity += GridComponent(Config.GRID_CELL_SIZE_F)
}
