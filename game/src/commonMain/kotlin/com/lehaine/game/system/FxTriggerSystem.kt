package com.lehaine.game.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.game.Assets
import com.lehaine.game.component.FxTrigger
import com.lehaine.game.component.RenderLayer
import com.lehaine.littlekt.extras.ecs.component.Particles
import com.littlekt.graphics.g2d.ParticleSimulator
import com.littlekt.graphics.g2d.TextureSlice
import com.littlekt.graphics.webgpu.BlendState
import com.littlekt.math.PI2_F
import com.littlekt.math.geom.cosine
import com.littlekt.math.geom.radians
import com.littlekt.math.geom.sine
import com.littlekt.math.random
import com.littlekt.util.seconds

class FxTriggerSystem(private val particleSimulator: ParticleSimulator) :
    IteratingSystem(
        family = family { all(FxTrigger) }
    ) {
    private lateinit var bgAdd: Entity
    private lateinit var bgNormal: Entity

    private lateinit var topAdd: Entity
    private lateinit var topNormal: Entity

    override fun onInit() {
        bgAdd = world.entity {
            it += Particles().apply {
                blendMode = BlendState.Add
            }
            it += RenderLayer.BACKGROUND
        }
        bgNormal = world.entity {
            it += Particles()
            it += RenderLayer.BACKGROUND
        }

        topAdd = world.entity {
            it += Particles().apply {
                blendMode = BlendState.Add
            }
            it += RenderLayer.FOREGROUND
        }
        topNormal = world.entity {
            it += Particles()
            it += RenderLayer.FOREGROUND
        }
    }


    override fun onTickEntity(entity: Entity) {
        val trigger = entity[FxTrigger]

        when (trigger.fxType) {
            FxTrigger.FxType.NONE -> {
                // NO-OP
            }

            FxTrigger.FxType.BLOOD -> {
                blood(
                    trigger.x ?: error("Missing x-coord for blood"),
                    trigger.y ?: error("Missing y-coord for blood"),
                    trigger.from.cosine,
                    trigger.from.sine
                )
            }
        }

        entity.configure {
            it -= FxTrigger
        }
    }

    private fun blood(x: Float, y: Float, xDir: Float, yDir: Float) {
        create(10) {
            val p = allocBgNormal(Assets.atlas.getByPrefix("fxDot").slice, x, y)
            p.color.set((0.45f..1f).random(), 0f, 0f, (0.2f..1f).random())
            p.xDelta = xDir * (1f..3f).random()
            p.yDelta = yDir * (1f..3f).random()
            p.rotation = (0f..PI2_F).random().radians
            p.friction = (0.92f..0.96f).random()
            p.life = (5..10).random().seconds
        }
    }

    private fun allocTopNormal(slice: TextureSlice, x: Float, y: Float) =
        particleSimulator.alloc(slice, x, y).also { with(world) { topNormal[Particles].add(it) } }

    private fun allocTopAdd(slice: TextureSlice, x: Float, y: Float) =
        particleSimulator.alloc(slice, x, y).also { with(world) { topAdd[Particles].add(it) } }

    private fun allocBgNormal(slice: TextureSlice, x: Float, y: Float) =
        particleSimulator.alloc(slice, x, y).also { with(world) { bgNormal[Particles].add(it) } }

    private fun allocBgAdd(slice: TextureSlice, x: Float, y: Float) =
        particleSimulator.alloc(slice, x, y).also { with(world) { bgAdd[Particles].add(it) } }

    private fun create(num: Int, createParticle: (index: Int) -> Unit) {
        for (i in 0 until num) {
            createParticle(i)
        }
    }
}