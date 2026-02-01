package com.lehaine.game.scene

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import com.lehaine.game.*
import com.lehaine.game.component.FxTrigger
import com.lehaine.game.component.LDtkLayerComponent
import com.lehaine.game.component.RenderLayer
import com.lehaine.game.entity.*
import com.lehaine.game.event.GameEvent
import com.lehaine.game.system.*
import com.lehaine.game.system.render.RenderGroupStage
import com.lehaine.game.system.render.RenderPipeline
import com.lehaine.game.system.render.pipeline.RenderCrtPipeline
import com.lehaine.game.system.render.pipeline.RenderSceneGraphPipeline
import com.lehaine.game.system.render.pipeline.RenderScenePipeline
import com.lehaine.game.system.render.pipeline.RenderSceneToRenderTargetPipeline
import com.lehaine.game.system.render.stage.*
import com.lehaine.littlekt.extras.FixedScene
import com.lehaine.littlekt.extras.ecs.component.Grid
import com.lehaine.littlekt.extras.ecs.component.GridCollisionResult.Companion.addGridCollisionResultPools
import com.lehaine.littlekt.extras.ecs.component.GridEntityCollisionResult.Companion.addGridEntityCollisionResultPools
import com.lehaine.littlekt.extras.ecs.component.addPool
import com.lehaine.littlekt.extras.ecs.event.EventBus
import com.lehaine.littlekt.extras.ecs.system.*
import com.lehaine.littlekt.extras.graphics.PixelSmoothRenderTarget
import com.littlekt.Context
import com.littlekt.file.ldtk.LDtkMapLoader
import com.littlekt.graph.SceneGraph
import com.littlekt.graph.node.ui.*
import com.littlekt.graph.sceneGraph
import com.littlekt.graphics.VAlign
import com.littlekt.graphics.g2d.Batch
import com.littlekt.graphics.g2d.ParticleSimulator
import com.littlekt.graphics.g2d.shape.ShapeRenderer
import com.littlekt.graphics.g2d.tilemap.ldtk.LDtkWorld
import com.littlekt.graphics.g2d.util.CameraSpriteBuffers
import com.littlekt.input.InputMapController
import com.littlekt.input.Key
import com.littlekt.math.Rect
import com.littlekt.util.datastructure.fastForEach
import com.littlekt.util.datastructure.forEachReversed
import com.littlekt.util.seconds
import com.littlekt.util.viewport.ExtendViewport
import com.littlekt.util.viewport.ScreenViewport
import kotlin.time.Duration

/**
 * @author Colton Daily
 * @date 3/1/2023
 */
class GameScene(context: Context, val batch: Batch, val shapeRenderer: ShapeRenderer) :
    FixedScene(context) {

    private val eventBus: EventBus = EventBus()

    private val sceneCamera: GridEntityCamera =
        GridEntityCamera().apply {
            renderTarget =
                PixelSmoothRenderTarget(context.graphics.width, context.graphics.height, Config.TARGET_HEIGHT)
        }
    private val sceneCameraViewBounds: Rect = Rect()
    private val cameraBuffers = CameraSpriteBuffers(context.graphics.device)
    private var pipelines: MutableList<RenderPipeline> = mutableListOf()

    private val controller =
        InputMapController<GameInput>(context.input).apply { setupController() }

    private val ui: Control
    private val graph: SceneGraph<GameInput> =
        sceneGraph(
            context = context,
            viewport = ExtendViewport(960, 540),
            batch = batch,
            uiInputSignals = createUiGameInputSignals(),
            whitePixel = Assets.atlas.getByPrefix("fxPixel").slice
        ) {
            ui = control {
                name = "UI"
                anchorRight = 1f
                anchorTop = 1f

                paddedContainer {
                    anchor(Control.AnchorLayout.TOP_LEFT)
                    padding(10)
                    column {
                        label {
                            font = Assets.pixelFont
                            verticalAlign = VAlign.TOP
                            onUpdate += { text = "FPS: ${context.stats.fps.toInt()}" }
                        }

                        label {
                            font = Assets.pixelFont
                            verticalAlign = VAlign.TOP
                            onUpdate += { text = "Debug mode: $debug ('K' to toggle)" }
                        }
                    }
                }
            }
        }
    private val particleSimulator: ParticleSimulator = ParticleSimulator(2048)

    val world: World = configureWorld {
        pipelines = initRenderPipelines()

        injectables {
            addGridCollisionResultPools()
            addGridEntityCollisionResultPools()
            addPool(FxTrigger) { FxTrigger() }
        }

        systems {
            run debugging@{ addDroneSystems(controller, eventBus) }

            run camera@{ add(GridCameraUpdaterSystem(sceneCamera)) }

            run updates@{
                add(CooldownSystem())
                add(GridTransformSyncSystem())
            }

            run gridPhysics@{
                add(GridMoveSystem())
                addPlatformerSystems()
            }

            run graphics@{
                add(AnimationSystem())
                add(SpriteRenderBoundsCalculationSystem())
                add(ParticleSimulatorSystem(particleSimulator))
                add(FxTriggerSystem(particleSimulator, eventBus))
            }

            run ui@{ add(UpdateSceneGraphSystem(graph)) }

            run hero@{ addHeroSystems(controller, eventBus) }

            run cleanup@{ add(GridCollisionCleanupSystem()) }

            run render@{
                // render game
                add(
                    RenderSystem(
                        batch = batch,
                        context = context,
                        pipelines = pipelines,
                    )
                )
            }
        }
    }

    val mapLoader: LDtkMapLoader by
    Assets.provider.load<LDtkMapLoader>(context.resourcesVfs["world.ldtk"])
    val map: LDtkWorld by Assets.provider.prepare { mapLoader.loadMap(false) }
    val level: Level by Assets.provider.prepare { Level(map.levels[0]) }

    private var debugger: Entity = Entity.NONE
    private var hero: Entity = Entity.NONE

    private var debug = false

    init {
        eventBus.register<GameEvent.ToggleDebug> {
            debug = !debug
            if (debug) {
                eventBus.send(GameEvent.LockController(ControllerOwner.DEBUG))
                with(world) { sceneCamera.follow(debugger[Grid]) }
            } else {
                eventBus.send(GameEvent.LockController(ControllerOwner.PLAYER))
                with(world) { sceneCamera.follow(hero[Grid]) }
            }
        }
        Assets.provider.prepare {
            initLevel()
            eventBus.send(GameEvent.LockController(ControllerOwner.PLAYER))
        }
    }

    private fun initRenderPipelines(): MutableList<RenderPipeline> {
        val pipelines = mutableListOf<RenderPipeline>()
        pipelines +=
            RenderSceneToRenderTargetPipeline(
                context = context,
                cameraBuffers = cameraBuffers,
                sceneViewport = ScreenViewport(context.graphics.width, context.graphics.height, camera = sceneCamera),
                sceneCameraViewBounds = sceneCameraViewBounds,
                stages =
                    listOf(
                        // background
                        RenderGroupStage(
                            listOf(
                                RenderLDtkLayerStage(context, cameraBuffers, RenderLayer.BACKGROUND),
                                RenderParticlesStage(
                                    sceneCameraViewBounds,
                                    RenderLayer.BACKGROUND,
                                ),
                            )
                        ),

                        // main
                        RenderGroupStage(
                            listOf(
                                RenderLDtkLayerStage(context, cameraBuffers, RenderLayer.MAIN),
                                RenderSpritesStage(sceneCameraViewBounds)
                            )
                        ),

                        // foreground
                        RenderGroupStage(
                            listOf(
                                RenderParticlesStage(
                                    sceneCameraViewBounds,
                                    RenderLayer.FOREGROUND,
                                ),
                                RenderLDtkLayerStage(context, cameraBuffers, RenderLayer.FOREGROUND),

                                )
                        ),

                        // debug
                        DebugRenderSpritesStage(sceneCameraViewBounds, eventBus),
                        DebugRenderBoundsStage(shapeRenderer, sceneCameraViewBounds, eventBus),
                    ),
            )

        pipelines +=
            RenderScenePipeline(
                context = context,
                sceneCamera = sceneCamera,
            )

        pipelines += RenderSceneGraphPipeline(context, graph)
        pipelines += RenderCrtPipeline(context)

        return pipelines
    }

    private fun initLevel() {
        var idx = 0
        map.levels[0].layers.forEachReversed { layer ->
            world.entity {
                it += LDtkLayerComponent(layer, idx++)
                it += RenderLayer.BACKGROUND
            }
        }
        with(world) {
            debugger = world.debugDrone(Assets.atlas.getByPrefix("fxPixel").slice)
            debugger[Grid].toGridPosition(15, 15)
            hero = world.hero(map.levels[0].entities("Hero").first(), level)
            sceneCamera.follow(hero[Grid], true)
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
        sceneCamera.renderTarget =
            PixelSmoothRenderTarget(context.graphics.width, context.graphics.height, Config.TARGET_HEIGHT)
        pipelines.fastForEach { it.resize(width, height) }
    }

    override fun Context.update(dt: Duration) {
        if (!Assets.provider.fullyLoaded) return

        if (input.isKeyJustPressed(Key.K)) {
            eventBus.send(GameEvent.ToggleDebug)
        }

        if (input.isKeyPressed(Key.SHIFT_LEFT) && input.isKeyJustPressed(Key.R)) {
            world.removeAll()
            eventBus.send(GameEvent.ResetWorld)
            initLevel()
            return
        }

        world.update(dt.seconds)
    }

    override fun Context.release() {
        graph.release()
        world.removeAll()
        mapLoader.release()
        pipelines.forEach { it.release() }
    }
}
