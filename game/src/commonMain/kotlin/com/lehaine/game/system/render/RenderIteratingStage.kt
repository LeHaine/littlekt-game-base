package com.lehaine.game.system.render

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.collection.EntityComparator

/**
 * @author Colton Daily
 * @date 3/15/2023
 */
abstract class RenderIteratingStage(
    val family: Family,
    private val comparator: EntityComparator = EMPTY_COMPARATOR,
    private val sortingType: SortingType = Automatic,
) : RenderStage {

    /**
     * Flag that defines if sorting of [entities][Entity] will be performed the next time [onTick] is called.
     *
     * If a [comparator] is defined and [sortingType] is [Automatic] then this flag is always true.
     *
     * Otherwise, it must be set programmatically to perform sorting. The flag gets cleared after sorting.
     */
    var doSort = sortingType == Automatic && comparator != EMPTY_COMPARATOR

    override fun render() {
        if (doSort) {
            doSort = sortingType == Automatic
            family.sort(comparator)
        }

        family.forEach { onRenderEntity(it) }
    }

    /**
     * Function that contains the update logic for each [entity][Entity] of the system.
     */
    abstract fun EntityComponentContext.onRenderEntity(entity: Entity)

    companion object {
        private val EMPTY_COMPARATOR = object : EntityComparator {
            override fun compare(entityA: Entity, entityB: Entity): Int = 0
        }
    }
}