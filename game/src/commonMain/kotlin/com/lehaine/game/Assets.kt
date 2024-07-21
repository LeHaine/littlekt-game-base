package com.lehaine.game

import com.littlekt.AssetProvider
import com.littlekt.BitmapFontAssetParameter
import com.littlekt.Context
import com.littlekt.Releasable
import com.littlekt.graphics.g2d.TextureAtlas
import com.littlekt.graphics.g2d.font.BitmapFont
import kotlin.concurrent.Volatile
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class Assets private constructor(context: Context) : Releasable {
    private val provider = AssetProvider(context)
    private val atlas: TextureAtlas by provider.load(context.resourcesVfs["tiles.atlas.json"])
    private val pixelFont: BitmapFont by provider.prepare {
        provider.loadSuspending<BitmapFont>(
            context.resourcesVfs["m5x7_16_outline.fnt"],
            BitmapFontAssetParameter(preloadedTextures = listOf(atlas["m5x7_16_outline_0"].slice))
        ).content
    }

    override fun release() {
        atlas.release()
        pixelFont.release()
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
            context.onUpdate { INSTANCE.provider.update() }
            return newInstance
        }

        fun release() {
            instance?.release()
        }
    }
}