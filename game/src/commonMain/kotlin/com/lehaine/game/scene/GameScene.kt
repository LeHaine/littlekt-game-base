package com.lehaine.game.scene

import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.world
import com.lehaine.game.*
import com.lehaine.game.component.LDtkLayerComponent
import com.lehaine.game.component.RenderLayer
import com.lehaine.game.component.RenderLayerComponent
import com.lehaine.game.entity.addDroneSystems
import com.lehaine.game.entity.debugDrone
import com.lehaine.game.event.ToggleDebug
import com.lehaine.game.system.GridCameraUpdaterSystem
import com.lehaine.game.system.ParticleSimulatorSystem
import com.lehaine.game.system.RenderSystem
import com.lehaine.game.system.UpdateAndRenderSceneGraphSystem
import com.lehaine.game.system.render.stage.*
import com.lehaine.littlekt.Context
import com.lehaine.littlekt.extras.FixedScene
import com.lehaine.littlekt.extras.ecs.component.GridCollisionResultComponent
import com.lehaine.littlekt.extras.ecs.component.GridComponent
import com.lehaine.littlekt.extras.ecs.event.EventBus
import com.lehaine.littlekt.extras.ecs.system.*
import com.lehaine.littlekt.extras.graphics.PixelSmoothFrameBuffer
import com.lehaine.littlekt.file.ldtk.LDtkMapLoader
import com.lehaine.littlekt.graph.node.ui.Control
import com.lehaine.littlekt.graph.node.ui.column
import com.lehaine.littlekt.graph.node.ui.control
import com.lehaine.littlekt.graph.node.ui.label
import com.lehaine.littlekt.graph.sceneGraph
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.g2d.ParticleSimulator
import com.lehaine.littlekt.graphics.g2d.TextureSlice
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.input.Key
import com.lehaine.littlekt.math.Rect
import com.lehaine.littlekt.util.datastructure.Pool
import com.lehaine.littlekt.util.seconds
import com.lehaine.littlekt.util.viewport.ExtendViewport
import com.lehaine.littlekt.util.viewport.ScreenViewport
import kotlin.time.Duration

/**
 * @author Colton Daily
 * @date 3/1/2023
 */
class GameScene(context: Context, val batch: Batch, val shapeRenderer: ShapeRenderer) : FixedScene(context) {

    private val eventBus = EventBus()

    private var sceneFbo =
        PixelSmoothFrameBuffer(context.graphics.width, context.graphics.height, 160).also { it.prepare(context) }
    private var sceneFboSlice = TextureSlice(
        sceneFbo.colorBufferTexture,
        0,
        sceneFbo.height - sceneFbo.pxHeight,
        sceneFbo.pxWidth,
        sceneFbo.pxHeight
    )
    private val sceneCamera = GridEntityCamera().apply {
        fbo = sceneFbo
    }
    private val sceneViewport = ScreenViewport(context.graphics.width, context.graphics.height, camera = sceneCamera)
    private val screenViewport = ScreenViewport(context.graphics.width, context.graphics.height)

    private val sceneCameraViewBounds = Rect()

    private lateinit var renderSceneToFboStage: RenderSceneToFboStage
    private lateinit var renderSceneStage: RenderSceneStage

    private val ui: Control
    private val graph =
        sceneGraph(
            context,
            ExtendViewport(960, 540),
            batch,
            uiInputSignals = createUiGameInputSignals(),
            whitePixel = Assets.atlas.getByPrefix("fxPixel").slice
        ) {
            ui = control {
                name = "UI"
                anchorRight = 1f
                anchorBottom = 1f

                column {
                    marginLeft = 10f
                    marginTop = 10f

                    label {
                        font = Assets.pixelFont
                        onUpdate += {
                            text = "FPS: ${context.stats.fps.toInt()}"
                        }
                    }

                    label {
                        font = Assets.pixelFont
                        var debug = false
                        eventBus.register<ToggleDebug> {
                            debug = !debug
                        }
                        onUpdate += {
                            text = "Debug mode: $debug ('K' to toggle)"
                        }
                    }
                }

            }
        }
    private val particleSimulator = ParticleSimulator(2048)

    val world: World = world {
        val gridCollisionPool = Pool { GridCollisionResultComponent(GridCollisionResultComponent.Axes.X, 0) }

        initRenderStages()

        systems {
            run debugging@{
                addDroneSystems(context.input)
            }

            run camera@{
                add(GridCameraUpdaterSystem(sceneCamera))
            }

            run gridPhysics@{
                add(GridMoveSystem(gridCollisionPool))
                add(GridCollisionResolverSystem())
                add(GridCollisionCleanupSystem(gridCollisionPool))
            }

            run graphics@{
                add(AnimationSystem())
                add(SpriteRenderBoundsCalculationSystem())
                add(ParticleSimulatorSystem(particleSimulator))
            }

            run render@{
                // render game scene
                add(
                    RenderSystem(
                        context = context,
                        viewport = sceneViewport,
                        viewBounds = sceneCameraViewBounds,
                        stages = listOf(renderSceneToFboStage, renderSceneStage)
                    )
                )

                // render ui
                add(UpdateAndRenderSceneGraphSystem(batch, graph))
            }
        }
    }
    val fx: Fx = Fx(world, particleSimulator)

    val mapLoader by Assets.provider.load<LDtkMapLoader>(context.resourcesVfs["world.ldtk"])
    val map by Assets.provider.prepare { mapLoader.loadMap(true) }
    val level by Assets.provider.prepare { Level(map.levels[0]) }

    init {
        Assets.provider.prepare(::initLevel)
    }

    private fun initRenderStages() {
        renderSceneToFboStage = RenderSceneToFboStage(
            context = context,
            batch = batch,
            sceneFbo = sceneFbo,
            sceneViewport = sceneViewport,
            stages = listOf(
                // background
                RenderLDtkLayerStage(batch, sceneCameraViewBounds, RenderLayerComponent.Background),
                RenderParticlesStage(batch, sceneCameraViewBounds, RenderLayerComponent.Background),

                // main
                RenderLDtkLayerStage(batch, sceneCameraViewBounds, RenderLayerComponent.Main),
                RenderSpritesStage(batch, sceneCameraViewBounds),

                // foreground
                RenderParticlesStage(batch, sceneCameraViewBounds, RenderLayerComponent.Foreground),
                RenderLDtkLayerStage(batch, sceneCameraViewBounds, RenderLayerComponent.Foreground),

                // debug
                DebugRenderSpritesStage(batch, sceneCameraViewBounds, eventBus),
                DebugRenderBoundsStage(shapeRenderer, sceneCameraViewBounds, eventBus)
            )
        )

        renderSceneStage = RenderSceneStage(
            context = context,
            batch = batch,
            sceneFbo = sceneFbo,
            sceneFboSlice = sceneFboSlice,
            sceneCamera = sceneCamera,
            screenViewport = screenViewport
        )
    }

    private fun initLevel() {
        map.levels[0].layers.forEach { layer ->
            world.entity {
                it += LDtkLayerComponent(layer, 0)
                it += RenderLayerComponent(RenderLayer.BACKGROUND)
            }
        }
        val debugger = world.debugDrone(Assets.atlas.getByPrefix("fxPixel").slice)
        with(world) {
            debugger[GridComponent].toGridPosition(15, 15)

            sceneCamera.follow(debugger[GridComponent], true)
            sceneCamera.viewBounds.width = map.levels[0].pxWidth.toFloat()
            sceneCamera.viewBounds.height = map.levels[0].pxHeight.toFloat()
        }
    }

    override suspend fun Context.show() {
        graph.initialize()
        graph.resize(graphics.width, graphics.height, true)
    }

    override fun Context.resize(width: Int, height: Int) {
        graph.resize(width, height, true)
        sceneFbo.dispose()
        sceneFbo = PixelSmoothFrameBuffer(width, height, 160).also { it.prepare(this) }
        sceneFboSlice = TextureSlice(
            sceneFbo.colorBufferTexture,
            0,
            sceneFbo.height - sceneFbo.pxHeight,
            sceneFbo.pxWidth,
            sceneFbo.pxHeight
        )
        sceneViewport.update(sceneFbo.width, sceneFbo.height, context)
        screenViewport.update(width, height, this, true)
        sceneCamera.fbo = sceneFbo

        renderSceneToFboStage.updateFbo(sceneFbo)
        renderSceneStage.updateFboAndSlice(sceneFbo, sceneFboSlice)
    }


    override fun Context.render(dt: Duration) {
        if (!Assets.provider.fullyLoaded) return

        if (input.isKeyJustPressed(Key.K)) {
            eventBus.send(ToggleDebug)
        }

        if (input.isKeyPressed(Key.SHIFT_LEFT) && input.isKeyJustPressed(Key.R)) {
            initLevel()
            return
        }

        world.update(dt.seconds)
    }

    override fun Context.dispose() {
        graph.dispose()
    }
}