package com.lehaine.game.system.render.stage

import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityComponentContext
import com.github.quillraven.fleks.UniqueId
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.collection.compareEntityBy
import com.lehaine.game.component.LDtkLayerComponent
import com.lehaine.game.system.render.RenderIteratingStage
import com.littlekt.graphics.g2d.Batch
import com.littlekt.math.Rect

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
class RenderLDtkLayerStage(
    private val viewBounds: Rect,
    vararg extraTypes: UniqueId<*> = emptyArray()
) : RenderIteratingStage(
    family { all(LDtkLayerComponent, *extraTypes) },
    comparator = compareEntityBy(LDtkLayerComponent)
) {

    override fun EntityComponentContext.onRenderEntity(entity: Entity, batch: Batch) {
        val layerComponent = entity[LDtkLayerComponent]
        layerComponent.layer.render(batch, viewBounds)
    }
}