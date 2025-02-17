package com.lehaine.game.entity

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.lehaine.game.Config
import com.lehaine.game.component.DebugRenderBounds
import com.lehaine.littlekt.extras.ecs.component.Grid
import com.lehaine.littlekt.extras.ecs.component.RenderBounds
import com.lehaine.littlekt.extras.ecs.component.Sprite

/**
 * Adds a [Sprite], [RenderBounds], and [Grid] to the specified [entity].
 */
fun EntityCreateContext.addSpriteBundle(entity: Entity) {
    entity += Sprite()
    entity += RenderBounds()
    entity += DebugRenderBounds()
    entity += Grid(Config.GRID_CELL_SIZE_F)
}
