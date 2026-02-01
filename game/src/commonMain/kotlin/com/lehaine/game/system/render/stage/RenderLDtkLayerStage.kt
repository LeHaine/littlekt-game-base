package com.lehaine.game.system.render.stage

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityComponentContext
import com.github.quillraven.fleks.UniqueId
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.collection.compareEntityBy
import com.lehaine.game.component.LDtkLayerComponent
import com.lehaine.game.system.render.RenderIteratingCacheStage
import com.littlekt.Context
import com.littlekt.graphics.g2d.SpriteCache
import com.littlekt.graphics.shader.Shader
import com.littlekt.graphics.util.CameraBuffersViaMatrix
import com.littlekt.graphics.webgpu.RenderPassEncoder
import com.littlekt.math.Mat4

/**
 * @author Colton Daily
 * @date 3/13/2023
 */
class RenderLDtkLayerStage(
    context: Context,
    cameraBuffers: CameraBuffersViaMatrix,
    vararg extraTypes: UniqueId<*> = emptyArray()
) : RenderIteratingCacheStage(
    family { all(LDtkLayerComponent, *extraTypes) },
    comparator = compareEntityBy(LDtkLayerComponent)
) {
    private val cache = SpriteCache(
        context.graphics.device,
        context.graphics.preferredFormat,
        cameraBuffers = cameraBuffers
    )

    override fun render(renderPassEncoder: RenderPassEncoder, viewProjection: Mat4) {
        super.render(renderPassEncoder, viewProjection)
        cache.render(renderPassEncoder, viewProjection)
    }

    override fun EntityComponentContext.onAddToCache(entity: Entity) {
        val layer = entity[LDtkLayerComponent]
        layer.layer.addToCache(cache, layer.layer.level.worldX.toFloat(), layer.layer.level.worldY.toFloat(), 1f)
    }

    override fun release() {
        cache.release()
    }
}