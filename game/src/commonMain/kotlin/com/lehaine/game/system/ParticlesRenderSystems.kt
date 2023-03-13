package com.lehaine.game.system

import com.lehaine.game.component.RenderLayerComponent
import com.lehaine.littlekt.extras.ecs.system.ParticlesRenderSystem
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.math.Rect

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
class ParticlesBackgroundRenderSystems(batch: Batch, viewBounds: Rect) :
    ParticlesRenderSystem(batch, viewBounds, RenderLayerComponent.Background) {
}

class ParticlesForegroundRenderSystems(batch: Batch, viewBounds: Rect) :
    ParticlesRenderSystem(batch, viewBounds, RenderLayerComponent.Foreground) {
}