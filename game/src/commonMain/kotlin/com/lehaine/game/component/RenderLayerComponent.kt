package com.lehaine.game.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.componentTypeOf

enum class RenderLayer {
    BACKGROUND,
    MAIN,
    FOREGROUND
}

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
class RenderLayerComponent(var layer: RenderLayer) : Component<RenderLayerComponent> {
    override fun type() = when (layer) {
        RenderLayer.BACKGROUND -> Background
        RenderLayer.MAIN -> Main
        RenderLayer.FOREGROUND -> Foreground
    }

    companion object {
        val Background = componentTypeOf<RenderLayerComponent>()
        val Main = componentTypeOf<RenderLayerComponent>()
        val Foreground = componentTypeOf<RenderLayerComponent>()
    }
}