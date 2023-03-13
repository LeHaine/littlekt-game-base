package com.lehaine.game.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.lehaine.littlekt.graphics.g2d.tilemap.ldtk.LDtkLayer

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
class LDtkLayerComponent(val layer: LDtkLayer) : Component<LDtkLayerComponent> {
    override fun type() = LDtkLayerComponent

    companion object : ComponentType<LDtkLayerComponent>()
}