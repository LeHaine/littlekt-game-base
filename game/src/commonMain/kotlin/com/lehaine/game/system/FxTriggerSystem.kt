package com.lehaine.game.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.game.Assets
import com.lehaine.game.component.FxTrigger
import com.lehaine.game.component.RenderLayer
import com.lehaine.game.event.GameEvent
import com.lehaine.game.util.randomSign
import com.lehaine.littlekt.extras.ecs.component.Particles
import com.lehaine.littlekt.extras.ecs.event.EventBus
import com.littlekt.graphics.g2d.ParticleSimulator
import com.littlekt.graphics.g2d.TextureSlice
import com.littlekt.graphics.webgpu.BlendState
import com.littlekt.math.geom.degrees
import com.littlekt.math.random
import kotlin.time.Duration.Companion.seconds

class FxTriggerSystem(private val particleSimulator: ParticleSimulator, eventBus: EventBus) :
    IteratingSystem(
        family = family { all(FxTrigger) }
    ) {
    private lateinit var bgAdd: Entity
    private lateinit var bgNormal: Entity

    private lateinit var topAdd: Entity
    private lateinit var topNormal: Entity

    init {
        eventBus.register<GameEvent.ResetWorld> { createParticleEntities() }
    }

    override fun onInit() {
        createParticleEntities()
    }

    private fun createParticleEntities() {
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

            FxTrigger.FxType.DUST -> {
                dust(trigger.x ?: error("Dust fx requires x-coord"), trigger.y ?: error("Dust fx requires y-coord"))
            }
        }

        entity.configure {
            it -= FxTrigger
        }
    }

    private fun dust(x: Float, y: Float) {
        create(8) {
            val p = allocTopNormal(
                Assets.atlas.getByPrefix("fxCircle").slice,
                x + (-600..600).random(),
                y + (-600..600).random()
            )
            p.rotation = (0..360).random().degrees
            p.scale((0.15f..1.2f).random())
            p.xDelta = (0.05f..0.1f).random().randomSign
            p.yDelta = (0.05f..0.1f).random().randomSign
            p.life = 8.seconds
            p.fadeInSpeed = 0.001f
            p.fadeOutSpeed = 0.001f
            p.color.set(0.25f, 0.25f, 0.25f, 0f)
            p.targetFadeAlpha = (0.1f..0.2f).random()
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