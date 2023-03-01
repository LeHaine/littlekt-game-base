package com.lehaine.game

import com.lehaine.littlekt.AssetProvider
import com.lehaine.littlekt.BitmapFontAssetParameter
import com.lehaine.littlekt.Context
import com.lehaine.littlekt.Disposable
import com.lehaine.littlekt.graphics.g2d.TextureAtlas
import com.lehaine.littlekt.graphics.g2d.font.BitmapFont
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.Volatile

class Assets private constructor(context: Context) : Disposable {
    private val provider = AssetProvider(context)
    private val atlas: TextureAtlas by provider.load(context.resourcesVfs["tiles.atlas.json"])
    private val pixelFont: BitmapFont by provider.prepare {
        provider.loadSuspending<BitmapFont>(
            context.resourcesVfs["m5x7_16_outline.fnt"],
            BitmapFontAssetParameter(preloadedTextures = listOf(atlas["m5x7_16_outline_0"].slice))
        ).content
    }

    override fun dispose() {
        atlas.dispose()
        pixelFont.dispose()
    }

    companion object {
        @Volatile
        private var instance: Assets? = null
        private val INSTANCE: Assets get() = instance ?: error("Instance has not been created!")

        val atlas: TextureAtlas get() = INSTANCE.atlas
        val pixelFont: BitmapFont get() = INSTANCE.pixelFont
        val provider: AssetProvider get() = INSTANCE.provider

        @OptIn(ExperimentalContracts::class)
        fun createInstance(context: Context, onLoad: () -> Unit): Assets {
            contract { callsInPlace(onLoad, InvocationKind.EXACTLY_ONCE) }
            check(instance == null) { "Instance already created!" }
            val newInstance = Assets(context)
            instance = newInstance
            INSTANCE.provider.onFullyLoaded = onLoad
            context.onRender { INSTANCE.provider.update() }
            return newInstance
        }

        fun dispose() {
            instance?.dispose()
        }
    }
}