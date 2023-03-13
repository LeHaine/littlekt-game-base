package com.lehaine.game.system

import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.game.component.LDtkLayerComponent
import com.lehaine.game.component.RenderLayerComponent
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.math.Rect

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
abstract class RenderLDtkLayerSystem(
    private val batch: Batch,
    private val viewBounds: Rect,
    vararg extraTypes: ComponentType<*> = emptyArray()
) :
    IteratingSystem(family { all(LDtkLayerComponent, *extraTypes) }) {

    override fun onTickEntity(entity: Entity) {
        val layerComponent = entity[LDtkLayerComponent]
        layerComponent.layer.render(batch, viewBounds)
    }
}

class RenderBackgroundLDtkLayerSystem(batch: Batch, viewBounds: Rect) :
    RenderLDtkLayerSystem(batch, viewBounds, RenderLayerComponent.Background)

class RenderMainLDtkLayerSystem(batch: Batch, viewBounds: Rect) :
    RenderLDtkLayerSystem(batch, viewBounds, RenderLayerComponent.Main)

class RenderForegroundLDtkLayerSystem(batch: Batch, viewBounds: Rect) :
    RenderLDtkLayerSystem(batch, viewBounds, RenderLayerComponent.Foreground)