package com.lehaine.game.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.game.component.LDtkLayerComponent
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.math.Rect

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
class RenderLDtkLayerSystem(private val batch: Batch, private val viewBounds: Rect) :
    IteratingSystem(family { all(LDtkLayerComponent) }) {

    override fun onTickEntity(entity: Entity) {
        val layerComponent = entity[LDtkLayerComponent]
        layerComponent.layer.render(batch, viewBounds)
    }
}