package com.lehaine.game.system.render

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.collection.EntityComparator
import com.littlekt.graphics.g2d.SpriteCache
import com.littlekt.graphics.webgpu.RenderPassEncoder
import com.littlekt.math.Mat4

/**
 * @author Colton Daily
 * @date 3/15/2023
 */
abstract class RenderIteratingCacheStage(
    val family: Family,
    private val comparator: EntityComparator = EMPTY_COMPARATOR,
    private val sortingType: SortingType = Automatic,
) : RenderStage.CacheStage {

    /**
     * Flag that defines if sorting of [entities][Entity] will be performed the next time [onTick] is called.
     *
     * If a [comparator] is defined and [sortingType] is [Automatic] then this flag is always true.
     *
     * Otherwise, it must be set programmatically to perform sorting. The flag gets cleared after sorting.
     */
    var doSort = sortingType == Automatic && comparator != EMPTY_COMPARATOR
    var added = false

    override fun render(renderPassEncoder: RenderPassEncoder, viewProjection: Mat4) {
        if (doSort) {
            doSort = sortingType == Automatic
            family.sort(comparator)
        }

        if (!added) {
            family.forEach { onAddToCache(it) }
            added = true
        }
    }

    /**
     * Function that contains the update logic for each [entity][Entity] of the system.
     */
    abstract fun EntityComponentContext.onAddToCache(entity: Entity)

    companion object {
        private val EMPTY_COMPARATOR = EntityComparator { _, _ -> 0 }
    }
}