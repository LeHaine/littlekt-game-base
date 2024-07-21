package com.lehaine.game

import com.lehaine.game.scene.LoadingScene
import com.lehaine.game.scene.MainMenuScene
import com.lehaine.littlekt.extras.FixedGame
import com.lehaine.littlekt.extras.FixedScene
import com.littlekt.Context
import com.littlekt.graphics.g2d.SpriteBatch
import com.littlekt.graphics.g2d.shape.ShapeRenderer
import com.littlekt.graphics.webgpu.PresentMode
import com.littlekt.graphics.webgpu.TextureUsage
import com.littlekt.input.Key

class GameCore(context: Context) : FixedGame<FixedScene>(context) {

    override suspend fun Context.start() {
        val surfaceCapabilities = graphics.surfaceCapabilities
        graphics.configureSurface(
            TextureUsage.RENDER_ATTACHMENT,
            graphics.preferredFormat,
            PresentMode.FIFO,
            surfaceCapabilities.alphaModes[0]
        )
        val batch = SpriteBatch(graphics.device, graphics, graphics.preferredFormat)
        val shapeRenderer: ShapeRenderer
        var initialized = false

        addScene(LoadingScene(this, batch))
        setScene<LoadingScene>()

        Assets.createInstance(this) {
            if (!initialized) {
                initialized = true
                shapeRenderer = ShapeRenderer(batch, Assets.atlas.getByPrefix("fxPixel").slice)
                addScene(MainMenuScene(context, batch, shapeRenderer, this@GameCore))
                setScene<MainMenuScene>()
                removeScene<LoadingScene>()?.release()
            }
        }

        onResize { _, _ ->
            graphics.configureSurface(
                TextureUsage.RENDER_ATTACHMENT,
                graphics.preferredFormat,
                PresentMode.FIFO,
                surfaceCapabilities.alphaModes[0]
            )
        }
        onUpdate {
            if (input.isKeyPressed(Key.SHIFT_LEFT) && input.isKeyJustPressed(Key.M)) {
                setScene<MainMenuScene>()
            }
            if (input.isKeyJustPressed(Key.ESCAPE)) {
                close()
            }
        }

        onPostUpdate {
            if (input.isKeyJustPressed(Key.P)) {
                logger.info { stats }
            }
        }

        onRelease {
            Assets.release()
        }
    }
}