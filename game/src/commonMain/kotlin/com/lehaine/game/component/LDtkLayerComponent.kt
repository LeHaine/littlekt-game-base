package com.lehaine.game.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.littlekt.graphics.g2d.tilemap.ldtk.LDtkLayer

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
class LDtkLayerComponent(val layer: LDtkLayer, val idx: Int) : Component<LDtkLayerComponent>,
    Comparable<LDtkLayerComponent> {

    override fun type() = LDtkLayerComponent

    override fun compareTo(other: LDtkLayerComponent): Int {
        return idx.compareTo(other.idx)
    }

    companion object : ComponentType<LDtkLayerComponent>()
}