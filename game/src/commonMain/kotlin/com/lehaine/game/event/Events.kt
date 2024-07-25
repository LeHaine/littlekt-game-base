package com.lehaine.game.event

import com.lehaine.game.ControllerOwner

sealed class GameEvent {
    data object ToggleDebug : GameEvent()

    data class LockController(val owner: ControllerOwner) : GameEvent()
}
