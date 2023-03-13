package com.lehaine.game

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.lehaine.game.component.RenderLayer
import com.lehaine.game.component.RenderLayerComponent
import com.lehaine.littlekt.extras.ecs.component.ParticlesComponent
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.g2d.ParticleSimulator
import com.lehaine.littlekt.graphics.g2d.TextureSlice
import com.lehaine.littlekt.graphics.toFloatBits
import com.lehaine.littlekt.graphics.util.BlendMode
import com.lehaine.littlekt.math.random
import com.lehaine.littlekt.util.seconds

/**
 * @author Colton Daily
 * @date 3/1/2023
 */
class Fx(private val world: World, private val particleSimulator: ParticleSimulator) {
    private val bgAdd: Entity = world.entity {
        it += ParticlesComponent().apply {
            blendMode = BlendMode.Add
        }
        it += RenderLayerComponent(RenderLayer.BACKGROUND)
    }
    private val bgNormal: Entity = world.entity {
        it += ParticlesComponent()
        it += RenderLayerComponent(RenderLayer.BACKGROUND)
    }

    private val topAdd: Entity = world.entity {
        it += ParticlesComponent().apply {
            blendMode = BlendMode.Add
        }
        it += RenderLayerComponent(RenderLayer.FOREGROUND)
    }
    private val topNormal: Entity = world.entity {
        it += ParticlesComponent()
        it += RenderLayerComponent(RenderLayer.FOREGROUND)
    }

    fun runDust(x: Float, y: Float, dir: Int) {
        create(5) {
            val p = allocTopNormal(Assets.atlas.getByPrefix("fxSmallCircle").slice, x, y)
            p.scale((0.15f..0.25f).random())
            p.color.set(DUST_COLOR)
            p.xDelta = (0.25f..0.75f).random() * dir
            p.yDelta = -(0.05f..0.15f).random()
            p.life = (0.05f..0.15f).random().seconds
            p.scaleDelta = (0.005f..0.015f).random()
        }
    }

    private fun allocTopNormal(slice: TextureSlice, x: Float, y: Float) =
        particleSimulator.alloc(slice, x, y).also { with(world) { topNormal[ParticlesComponent].add(it) } }

    private fun allocTopAdd(slice: TextureSlice, x: Float, y: Float) =
        particleSimulator.alloc(slice, x, y).also { with(world) { topAdd[ParticlesComponent].add(it) } }

    private fun allocBogNormal(slice: TextureSlice, x: Float, y: Float) =
        particleSimulator.alloc(slice, x, y).also { with(world) { bgNormal[ParticlesComponent].add(it) } }

    private fun allocBogAdd(slice: TextureSlice, x: Float, y: Float) =
        particleSimulator.alloc(slice, x, y).also { with(world) { bgAdd[ParticlesComponent].add(it) } }

    private fun create(num: Int, createParticle: (index: Int) -> Unit) {
        for (i in 0 until num) {
            createParticle(i)
        }
    }

    companion object {
        private val DUST_COLOR = Color.fromHex("#efddc0")
        private val DUST_COLOR_BITS = DUST_COLOR.toFloatBits()
    }
}