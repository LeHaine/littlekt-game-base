package com.lehaine.game.system.render

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.collection.EntityComparator
import com.littlekt.graphics.g2d.Batch
import com.littlekt.graphics.webgpu.CommandEncoder
import com.littlekt.graphics.webgpu.RenderPassDescriptor

/**
 * @author Colton Daily
 * @date 3/15/2023
 */
abstract class RenderIteratingBatchStage(
    val family: Family,
    private val comparator: EntityComparator = EMPTY_COMPARATOR,
    private val sortingType: SortingType = Automatic,
) : RenderStage.BatchStage {

    /**
     * Flag that defines if sorting of [entities][Entity] will be performed the next time [onTick] is called.
     *
     * If a [comparator] is defined and [sortingType] is [Automatic] then this flag is always true.
     *
     * Otherwise, it must be set programmatically to perform sorting. The flag gets cleared after sorting.
     */
    var doSort = sortingType == Automatic && comparator != EMPTY_COMPARATOR

    override fun render(batch: Batch, commandEncoder: CommandEncoder, renderPassDescriptor: RenderPassDescriptor) {
        if (doSort) {
            doSort = sortingType == Automatic
            family.sort(comparator)
        }

        family.forEach { onRenderEntity(it, batch) }
    }

    /**
     * Function that contains the update logic for each [entity][Entity] of the system.
     */
    abstract fun EntityComponentContext.onRenderEntity(entity: Entity, batch: Batch)

    companion object {
        private val EMPTY_COMPARATOR = EntityComparator { _, _ -> 0 }
    }
}