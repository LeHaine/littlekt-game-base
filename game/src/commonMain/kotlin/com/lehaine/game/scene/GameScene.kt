package com.lehaine.game.scene

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import com.lehaine.game.*
import com.lehaine.game.component.LDtkLayerComponent
import com.lehaine.game.component.RenderLayer
import com.lehaine.game.component.RenderLayerComponent
import com.lehaine.game.entity.*
import com.lehaine.game.event.GameEvent
import com.lehaine.game.system.GridCameraUpdaterSystem
import com.lehaine.game.system.ParticleSimulatorSystem
import com.lehaine.game.system.RenderSystem
import com.lehaine.game.system.UpdateSceneGraphSystem
import com.lehaine.game.system.render.stage.*
import com.lehaine.littlekt.extras.FixedScene
import com.lehaine.littlekt.extras.ecs.component.GridCollisionResultComponent
import com.lehaine.littlekt.extras.ecs.component.GridComponent
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
import com.littlekt.graphics.g2d.TextureSlice
import com.littlekt.graphics.g2d.shape.ShapeRenderer
import com.littlekt.graphics.g2d.tilemap.ldtk.LDtkWorld
import com.littlekt.input.InputMapController
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
class GameScene(context: Context, val batch: Batch, val shapeRenderer: ShapeRenderer) :
    FixedScene(context) {

    private val eventBus: EventBus = EventBus()

    private var sceneRenderTarget: PixelSmoothRenderTarget =
        PixelSmoothRenderTarget(
            context.graphics.device,
            context.graphics.preferredFormat,
            context.graphics.width,
            context.graphics.height,
            160
        )
    private var sceneFboSlice: TextureSlice =
        TextureSlice(
            sceneRenderTarget.target,
            0,
            sceneRenderTarget.height - sceneRenderTarget.pxHeight,
            sceneRenderTarget.pxWidth,
            sceneRenderTarget.pxHeight
        )
    private val sceneCamera: GridEntityCamera =
        GridEntityCamera().apply { renderTarget = sceneRenderTarget }
    private val sceneViewport: ScreenViewport =
        ScreenViewport(context.graphics.width, context.graphics.height, camera = sceneCamera)
    private val screenViewport: ScreenViewport =
        ScreenViewport(context.graphics.width, context.graphics.height)

    private val sceneCameraViewBounds: Rect = Rect()

    private lateinit var renderSceneToRenderTargetStage: RenderSceneToRenderTargetStage
    private lateinit var renderSceneStage: RenderSceneStage

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
        val gridCollisionPool = Pool {
            GridCollisionResultComponent(GridCollisionResultComponent.Axes.X, 0)
        }

        initRenderStages()

        systems {
            run debugging@{ addDroneSystems(controller, eventBus) }

            run camera@{ add(GridCameraUpdaterSystem(sceneCamera)) }

            run updates@{
                add(CooldownSystem())
                add(GridTransformSyncSystem())
            }

            run gridPhysics@{
                add(GridMoveSystem(gridCollisionPool))
                addPlatformerSystems()
            }

            run graphics@{
                add(AnimationSystem())
                add(SpriteRenderBoundsCalculationSystem())
                add(ParticleSimulatorSystem(particleSimulator))
            }

            run ui@{ add(UpdateSceneGraphSystem(graph)) }

            run hero@{ addHeroSystems(controller, eventBus) }

            run cleanup@{ add(GridCollisionCleanupSystem(gridCollisionPool)) }

            run render@{
                // render game
                add(
                    RenderSystem(
                        batch = batch,
                        context = context,
                        viewport = sceneViewport,
                        viewBounds = sceneCameraViewBounds,
                        stages =
                            listOf(
                                renderSceneToRenderTargetStage,
                                renderSceneStage,
                                RenderSceneGraphStage(graph)
                            )
                    )
                )
            }
        }
    }
    val fx: Fx = Fx(world, particleSimulator)

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
                with(world) { sceneCamera.follow(debugger[GridComponent]) }
            } else {
                eventBus.send(GameEvent.LockController(ControllerOwner.PLAYER))
                with(world) { sceneCamera.follow(hero[GridComponent]) }
            }
        }
        Assets.provider.prepare {
            initLevel()
            eventBus.send(GameEvent.LockController(ControllerOwner.PLAYER))
        }
    }

    private fun initRenderStages() {
        renderSceneToRenderTargetStage =
            RenderSceneToRenderTargetStage(
                renderTarget = sceneRenderTarget.target,
                sceneViewport = sceneViewport,
                stages =
                    listOf(
                        // background
                        RenderLDtkLayerStage(
                            sceneCameraViewBounds,
                            RenderLayerComponent.Background
                        ),
                        RenderParticlesStage(
                            sceneCameraViewBounds,
                            RenderLayerComponent.Background
                        ),

                        // main
                        RenderLDtkLayerStage(sceneCameraViewBounds, RenderLayerComponent.Main),
                        RenderSpritesStage(sceneCameraViewBounds),

                        // foreground
                        RenderParticlesStage(
                            sceneCameraViewBounds,
                            RenderLayerComponent.Foreground
                        ),
                        RenderLDtkLayerStage(
                            sceneCameraViewBounds,
                            RenderLayerComponent.Foreground
                        ),

                        // debug
                        DebugRenderSpritesStage(sceneCameraViewBounds, eventBus),
                        DebugRenderBoundsStage(shapeRenderer, sceneCameraViewBounds, eventBus)
                    )
            )

        renderSceneStage =
            RenderSceneStage(
                context = context,
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
        with(world) {
            debugger = world.debugDrone(Assets.atlas.getByPrefix("fxPixel").slice)
            debugger[GridComponent].toGridPosition(15, 15)
            hero = world.hero(map.levels[0].entities("Hero").first(), level)
            sceneCamera.follow(hero[GridComponent], true)
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
            PixelSmoothRenderTarget(
                context.graphics.device,
                context.graphics.preferredFormat,
                width,
                height,
                160
            )
        sceneFboSlice =
            TextureSlice(
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
            eventBus.send(GameEvent.ToggleDebug)
        }

        if (input.isKeyPressed(Key.SHIFT_LEFT) && input.isKeyJustPressed(Key.R)) {
            world.removeAll()
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
