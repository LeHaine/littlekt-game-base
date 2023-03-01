package com.lehaine.game

import com.lehaine.game.scene.GameScene
import com.lehaine.game.scene.LoadingScene
import com.lehaine.littlekt.Context
import com.lehaine.littlekt.async.KtScope
import com.lehaine.littlekt.extras.FixedGame
import com.lehaine.littlekt.extras.FixedScene
import com.lehaine.littlekt.graphics.g2d.SpriteBatch
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.input.Key
import kotlinx.coroutines.launch


class GameCore(context: Context) : FixedGame<FixedScene>(context) {


    override suspend fun Context.start() {
        val batch = SpriteBatch(this)
        val shapeRenderer: ShapeRenderer

        addScene(LoadingScene(this, batch))
        setScene<LoadingScene>()

        Assets.createInstance(this) {
            shapeRenderer = ShapeRenderer(batch, Assets.atlas.getByPrefix("fxPixel").slice)
            KtScope.launch {
                addScene(GameScene(context, batch, shapeRenderer))
                setScene<GameScene>()
                removeScene<LoadingScene>()?.dispose()
            }
        }
        onRender {
            if (input.isKeyJustPressed(Key.ESCAPE)) {
                close()
            }
        }

        onPostRender {
            if (input.isKeyJustPressed(Key.P)) {
                logger.info { stats }
            }
        }

        onDispose {
            Assets.dispose()
        }
    }
}