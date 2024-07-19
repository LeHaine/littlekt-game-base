package com.lehaine.game.scene

import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
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
import com.lehaine.game.system.UpdateSceneGraphSystem
import com.lehaine.game.system.render.stage.*
import com.lehaine.littlekt.extras.FixedScene
import com.lehaine.littlekt.extras.ecs.component.GridCollisionResultComponent
import com.lehaine.littlekt.extras.ecs.component.GridComponent
import com.lehaine.littlekt.extras.ecs.event.EventBus
import com.lehaine.littlekt.extras.ecs.system.AnimationSystem
import com.lehaine.littlekt.extras.ecs.system.GridCollisionCleanupSystem
import com.lehaine.littlekt.extras.ecs.system.GridMoveSystem
import com.lehaine.littlekt.extras.ecs.system.SpriteRenderBoundsCalculationSystem
import com.lehaine.littlekt.extras.graphics.PixelSmoothRenderTarget
import com.littlekt.Context
import com.littlekt.file.ldtk.LDtkMapLoader
import com.littlekt.graph.node.ui.Control
import com.littlekt.graph.node.ui.column
import com.littlekt.graph.node.ui.control
import com.littlekt.graph.node.ui.label
import com.littlekt.graph.sceneGraph
import com.littlekt.graphics.g2d.Batch
import com.littlekt.graphics.g2d.ParticleSimulator
import com.littlekt.graphics.g2d.TextureSlice
import com.littlekt.graphics.g2d.shape.ShapeRenderer
import com.littlekt.input.Key
import com.littlekt.math.Rect
import com.littlekt.util.datastructure.Pool
import com.littlekt.util.datastructure.forEachReversed
import com.littlekt.util.seconds
import com.littlekt.util.viewport.ExtendViewport
import com.littlekt.util.viewport.ScreenViewport
import kotlin.time.Duration

/**
 * @author Colton Daily
 * @date 3/1/2023
 */
class GameScene(context: Context, val batch: Batch, val shapeRenderer: ShapeRenderer) : FixedScene(context) {

    private val eventBus = EventBus()

    private var sceneRenderTarget = PixelSmoothRenderTarget(
        context.graphics.device, context.graphics.preferredFormat, context.graphics.width, context.graphics.height, 160
    )
    private var sceneFboSlice = TextureSlice(
        sceneRenderTarget.target,
        0,
        sceneRenderTarget.height - sceneRenderTarget.pxHeight,
        sceneRenderTarget.pxWidth,
        sceneRenderTarget.pxHeight
    )
    private val sceneCamera = GridEntityCamera().apply {
        renderTarget = sceneRenderTarget
    }
    private val sceneViewport = ScreenViewport(context.graphics.width, context.graphics.height, camera = sceneCamera)
    private val screenViewport = ScreenViewport(context.graphics.width, context.graphics.height)

    private val sceneCameraViewBounds = Rect()

    private lateinit var renderSceneToRenderTargetStage: RenderSceneToRenderTargetStage
    private lateinit var renderSceneStage: RenderSceneStage

    private val ui: Control
    private val graph = sceneGraph(
        context,
        ExtendViewport(960, 540),
        batch,
        uiInputSignals = createUiGameInputSignals(),
        whitePixel = Assets.atlas.getByPrefix("fxPixel").slice
    ) {
        ui = control {
            name = "UI"
            anchorRight = 1f
            anchorTop = 1f

            column {
                anchor(Control.AnchorLayout.TOP_LEFT)
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

    val world: World = configureWorld {
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
                add(GridCollisionCleanupSystem(gridCollisionPool))
            }

            run graphics@{
                add(AnimationSystem())
                add(SpriteRenderBoundsCalculationSystem())
                add(ParticleSimulatorSystem(particleSimulator))
            }

            run ui@{
                add(UpdateSceneGraphSystem(graph))
            }

            run render@{
                // render game
                add(
                    RenderSystem(
                        context = context,
                        viewport = sceneViewport,
                        viewBounds = sceneCameraViewBounds,
                        stages = listOf(
                            renderSceneToRenderTargetStage,
                            renderSceneStage,
                            RenderSceneGraphStage(batch, graph)
                        )
                    )
                )
            }
        }
    }
    val fx: Fx = Fx(world, particleSimulator)

    val mapLoader by Assets.provider.load<LDtkMapLoader>(context.resourcesVfs["world.ldtk"])
    val map by Assets.provider.prepare { mapLoader.loadMap(false) }
    val level by Assets.provider.prepare { Level(map.levels[0]) }

    init {
        Assets.provider.prepare(::initLevel)
    }

    private fun initRenderStages() {
        renderSceneToRenderTargetStage = RenderSceneToRenderTargetStage(
            batch = batch, renderTarget = sceneRenderTarget.target, sceneViewport = sceneViewport, stages = listOf(
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
            sceneRenderTarget = sceneRenderTarget,
            sceneRenderTargetSlice = sceneFboSlice,
            sceneCamera = sceneCamera,
            screenViewport = screenViewport
        )
    }

    private fun initLevel() {
        var idx = 0
        map.levels[0].layers.forEachReversed { layer ->
            world.entity {
                it += LDtkLayerComponent(layer, idx++)
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
        sceneRenderTarget.release()
        sceneRenderTarget =
            PixelSmoothRenderTarget(context.graphics.device, context.graphics.preferredFormat, width, height, 160)
        sceneFboSlice = TextureSlice(
            sceneRenderTarget.target,
            0,
            sceneRenderTarget.height - sceneRenderTarget.pxHeight,
            sceneRenderTarget.pxWidth,
            sceneRenderTarget.pxHeight
        )
        sceneViewport.update(sceneRenderTarget.width, sceneRenderTarget.height)
        screenViewport.update(width, height, true)
        sceneCamera.renderTarget = sceneRenderTarget

        renderSceneToRenderTargetStage.renderTarget = sceneRenderTarget.target
        renderSceneStage.updateRenderTargetAndSlice(sceneRenderTarget, sceneFboSlice)
    }

    override fun Context.update(dt: Duration) {
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

    override fun Context.release() {
        graph.release()
        world.removeAll()
        sceneRenderTarget.release()
        mapLoader.release()
    }
}