package com.lehaine.game.component

import com.github.quillraven.fleks.ComponentType
import com.lehaine.littlekt.extras.ecs.component.PoolType
import com.lehaine.littlekt.extras.ecs.component.PoolableComponent
import com.littlekt.math.geom.Angle

class FxTrigger(
    override val poolType: PoolType<FxTrigger> = FxTrigger
) : PoolableComponent<FxTrigger> {
    var from: Angle = Angle.ZERO
    var x: Float? = null
    var y: Float? = null
    var fxType: FxType = FxType.NONE

    enum class FxType {
        NONE,
        BLOOD
    }

    override fun type(): ComponentType<FxTrigger> = FxTrigger

    override fun reset() {
        from = Angle.ZERO
        fxType = FxType.NONE
        x = null
        y = null
    }

    companion object : ComponentType<FxTrigger>(), PoolType<FxTrigger> {
        override val poolName: String = "fxTriggerPool"
    }
}