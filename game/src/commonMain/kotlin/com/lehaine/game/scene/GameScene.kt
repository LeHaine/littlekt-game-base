package com.lehaine.game.scene

import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.world
import com.lehaine.game.*
import com.lehaine.game.component.LDtkLayerComponent
import com.lehaine.game.component.RenderLayer
import com.lehaine.game.component.RenderLayerComponent
import com.lehaine.game.entity.addDroneSystems
import com.lehaine.game.entity.debugDrone
import com.lehaine.game.system.*
import com.lehaine.littlekt.Context
import com.lehaine.littlekt.extras.FixedScene
import com.lehaine.littlekt.extras.ecs.component.GridCollisionResultComponent
import com.lehaine.littlekt.extras.ecs.component.GridComponent
import com.lehaine.littlekt.extras.ecs.system.*
import com.lehaine.littlekt.extras.graphics.PixelSmoothFrameBuffer
import com.lehaine.littlekt.extras.shader.PixelSmoothFragmentShader
import com.lehaine.littlekt.extras.shader.PixelSmoothVertexShader
import com.lehaine.littlekt.file.ldtk.LDtkMapLoader
import com.lehaine.littlekt.graph.node.ui.Control
import com.lehaine.littlekt.graph.node.ui.centerContainer
import com.lehaine.littlekt.graph.node.ui.control
import com.lehaine.littlekt.graph.node.ui.label
import com.lehaine.littlekt.graph.sceneGraph
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.g2d.ParticleSimulator
import com.lehaine.littlekt.graphics.g2d.TextureSlice
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.graphics.gl.ClearBufferMask
import com.lehaine.littlekt.graphics.gl.State
import com.lehaine.littlekt.graphics.shader.ShaderProgram
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

    private var sceneFbo =
        PixelSmoothFrameBuffer(context.graphics.width, context.graphics.height, 160).also { it.prepare(context) }
    private var sceneFboSlice = TextureSlice(
        sceneFbo.colorBufferTexture,
        0,
        sceneFbo.height - sceneFbo.pxHeight,
        sceneFbo.pxWidth,
        sceneFbo.pxHeight
    )
    private val pixelSmoothShader =
        ShaderProgram(PixelSmoothVertexShader(), PixelSmoothFragmentShader()).also { it.prepare(context) }
    private val sceneCamera = GridEntityCamera().apply {
        fbo = sceneFbo
    }
    private val sceneViewport = ScreenViewport(context.graphics.width, context.graphics.height, camera = sceneCamera)
    private val screenViewport = ScreenViewport(context.graphics.width, context.graphics.height)

    private val sceneCameraViewBounds = Rect()

    private lateinit var renderSceneFboStartSystem: RenderSceneFboStartSystem
    private lateinit var renderSceneFboEndSystem: RenderSceneFboEndSystem
    private lateinit var renderSceneFboSystem: RenderSceneFboSystem

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

                centerContainer {
                    anchorRight = 1f
                    anchorBottom = 1f
                    label {
                        text = "TODO: Implement game logic"
                        font = Assets.pixelFont
                    }
                }
            }
        }
    private val particleSimulator = ParticleSimulator(2048)

    val world: World = world {
        val gridCollisionPool = Pool { GridCollisionResultComponent(GridCollisionResultComponent.Axes.X, 0) }
        renderSceneFboStartSystem = RenderSceneFboStartSystem(context, batch, sceneFbo, sceneViewport)
        renderSceneFboEndSystem = RenderSceneFboEndSystem(batch, sceneFbo)
        renderSceneFboSystem =
            RenderSceneFboSystem(
                context,
                batch,
                sceneFbo,
                sceneFboSlice,
                pixelSmoothShader,
                sceneCamera,
                screenViewport
            )

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

            // render scene
            run renderScene@{
                add(renderSceneFboStartSystem)
                add(CameraViewBoundsCalculatorSystem(sceneCamera, sceneCameraViewBounds))

                // background related items
                run backgroundItems@{
                    add(ParticlesBackgroundRenderSystems(batch, sceneCameraViewBounds))
                    add(RenderBackgroundLDtkLayerSystem(batch, sceneCameraViewBounds))
                }

                // main related items
                run mainItems@{
                    add(RenderMainLDtkLayerSystem(batch, sceneCameraViewBounds))
                    add(RenderSceneSystem(batch, sceneCameraViewBounds))
                }

                // foreground related items
                run foregroundItems@{
                    add(RenderForegroundLDtkLayerSystem(batch, sceneCameraViewBounds))
                    add(ParticlesForegroundRenderSystems(batch, sceneCameraViewBounds))
                }
                add(renderSceneFboEndSystem)
            }

            // render scene fbo
            run sceneFbo@{
                add(renderSceneFboSystem)
            }

            // render ui
            run ui@{
                add(UpdateAndRenderSceneGraphSystem(batch, graph))
            }
        }
    }
    val fx: Fx = Fx(world, particleSimulator)

    val mapLoader by Assets.provider.load<LDtkMapLoader>(context.resourcesVfs["world.ldtk"])
    val map by Assets.provider.prepare { mapLoader.loadMap(true) }
    val level by Assets.provider.prepare { Level(map.levels[0]) }

    init {
        Assets.provider.prepare {
            map.levels[0].layers.forEach { layer ->
                world.entity {
                    it += LDtkLayerComponent(layer)
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

        renderSceneFboStartSystem.setFbo(sceneFbo)
        renderSceneFboEndSystem.setFbo(sceneFbo)
        renderSceneFboSystem.updateFboAndSlice(sceneFbo, sceneFboSlice)
    }


    override fun Context.render(dt: Duration) {
        if (!Assets.provider.fullyLoaded) return
        gl.enable(State.SCISSOR_TEST)
        gl.scissor(0, 0, graphics.width, graphics.height)
        gl.clearColor(0.1f, 0.1f, 0.1f, 1f)
        gl.clear(ClearBufferMask.COLOR_BUFFER_BIT)

        world.update(dt.seconds)
    }

    override fun Context.dispose() {
        graph.dispose()
    }
}