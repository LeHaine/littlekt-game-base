package com.lehaine.game.scene

import com.lehaine.game.*
import com.lehaine.game.entity.DebugEntity
import com.lehaine.littlekt.Context
import com.lehaine.littlekt.extras.FixedScene
import com.lehaine.littlekt.extras.entity.toGridPosition
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
import com.lehaine.littlekt.graphics.g2d.TextureSlice
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.graphics.g2d.use
import com.lehaine.littlekt.graphics.gl.ClearBufferMask
import com.lehaine.littlekt.graphics.gl.State
import com.lehaine.littlekt.graphics.shader.ShaderProgram
import com.lehaine.littlekt.graphics.use
import com.lehaine.littlekt.util.viewport.ExtendViewport
import com.lehaine.littlekt.util.viewport.ScreenViewport
import kotlin.time.Duration

/**
 * @author Colton Daily
 * @date 3/1/2023
 */
class GameScene(context: Context, val batch: Batch, val shapeRenderer: ShapeRenderer) : FixedScene(context) {

    private val fx = Fx()
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
    private val sceneCamera = EntityCamera().apply {
        fbo = sceneFbo
    }
    private val sceneViewport = ScreenViewport(context.graphics.width, context.graphics.height, camera = sceneCamera)
    private val screenViewport = ScreenViewport(context.graphics.width, context.graphics.height)

    private val ui: Control
    private val graph =
        sceneGraph(context, ExtendViewport(960, 540), batch, uiInputSignals = createUiGameInputSignals()) {
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

    val mapLoader by Assets.provider.load<LDtkMapLoader>(context.resourcesVfs["world.ldtk"])
    val world by Assets.provider.prepare { mapLoader.loadMap(true) }
    val level by Assets.provider.prepare { Level(world.levels[0]) }
    private val debugger by Assets.provider.prepare { DebugEntity(context) }

    init {
        Assets.provider.prepare {
            debugger.toGridPosition(15, 15)
            sceneCamera.targetZoom= 2f
            sceneCamera.follow(debugger, true)
            sceneCamera.viewBounds.width = world.levels[0].pxWidth.toFloat()
            sceneCamera.viewBounds.height = world.levels[0].pxHeight.toFloat()
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
    }

    override fun Context.fixedUpdate() {
        if (!Assets.provider.fullyLoaded) return

        debugger.fixedUpdate()
    }

    override fun Context.render(dt: Duration) {
        if (!Assets.provider.fullyLoaded) return
        gl.enable(State.SCISSOR_TEST)
        gl.scissor(0, 0, graphics.width, graphics.height)
        gl.clearColor(0.1f, 0.1f, 0.1f, 1f)
        gl.clear(ClearBufferMask.COLOR_BUFFER_BIT)

        // update fx, camera, and entities
        debugger.fixedProgressionRatio = fixedProgressionRatio
        debugger.preUpdate(dt)
        debugger.update(dt)

        fx.update(dt)

        sceneCamera.tmod = tmod
        sceneCamera.update(dt)
        debugger.postUpdate(dt)


        // render scene
        sceneViewport.apply(this)
        sceneFbo.use {
            gl.clear(ClearBufferMask.COLOR_BUFFER_BIT)
            batch.useDefaultShader()
            batch.use(sceneCamera.viewProjection) {
                // render background
                fx.bgNormal.render(batch, sceneCamera, shapeRenderer)
                fx.bgAdd.render(batch, sceneCamera, shapeRenderer)

                // render main
                world.render(batch, sceneCamera)
                debugger.render(batch, sceneCamera, shapeRenderer)

                // render top
                fx.bgNormal.render(batch, sceneCamera, shapeRenderer)
                fx.bgAdd.render(batch, sceneCamera, shapeRenderer)
            }
        }

        // render pixel smooth frame buffer
        screenViewport.apply(this)
        batch.shader = pixelSmoothShader
        batch.use(screenViewport.camera.viewProjection) {
            pixelSmoothShader.vertexShader.uTextureSizes.apply(
                pixelSmoothShader,
                sceneFbo.width.toFloat(),
                sceneFbo.height.toFloat(),
                0f,
                0f
            )
            pixelSmoothShader.vertexShader.uSampleProperties.apply(
                pixelSmoothShader, 0f, 0f, sceneCamera.scaledDistX, sceneCamera.scaledDistY
            )
            batch.draw(
                sceneFboSlice,
                0f,
                0f,
                width = context.graphics.width.toFloat(),
                height = context.graphics.height.toFloat(),
                flipY = true
            )
        }
        batch.useDefaultShader()

        // render UI
        graph.update(dt)
        graph.render()
    }

    override fun Context.dispose() {
        graph.dispose()
    }
}