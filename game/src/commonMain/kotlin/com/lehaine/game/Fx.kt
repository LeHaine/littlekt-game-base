package com.lehaine.game

import com.lehaine.littlekt.extras.renderable.ParticleBatch
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.g2d.ParticleSimulator
import com.lehaine.littlekt.graphics.g2d.TextureSlice
import com.lehaine.littlekt.graphics.toFloatBits
import com.lehaine.littlekt.graphics.util.BlendMode
import com.lehaine.littlekt.math.random
import com.lehaine.littlekt.util.seconds
import kotlin.time.Duration

/**
 * @author Colton Daily
 * @date 3/1/2023
 */
class Fx {
    private val particleSimulator = ParticleSimulator(2048)

    val bgAdd = ParticleBatch().apply {
        blendMode = BlendMode.Add
    }

    val bgNormal = ParticleBatch()
    val topAdd = ParticleBatch().apply {
        blendMode = BlendMode.Add
    }
    val topNormal = ParticleBatch()

    fun update(dt: Duration, tmod: Float = -1f) {
        particleSimulator.update(dt, tmod)
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
        particleSimulator.alloc(slice, x, y).also { topNormal.add(it) }

    private fun allocTopAdd(slice: TextureSlice, x: Float, y: Float) =
        particleSimulator.alloc(slice, x, y).also { topAdd.add(it) }

    private fun allocBogNormal(slice: TextureSlice, x: Float, y: Float) =
        particleSimulator.alloc(slice, x, y).also { bgNormal.add(it) }

    private fun allocBogAdd(slice: TextureSlice, x: Float, y: Float) =
        particleSimulator.alloc(slice, x, y).also { bgAdd.add(it) }

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