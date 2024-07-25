package com.lehaine.game

import com.littlekt.graph.SceneGraph
import com.littlekt.input.*

enum class GameInput {
    UI_ACCEPT,
    UI_SELECT,
    UI_CANCEL,
    UI_UP,
    UI_DOWN,
    UI_LEFT,
    UI_RIGHT,
    UI_HOME,
    UI_END,
    UI_FOCUS_NEXT,
    UI_FOCUS_PREV,
    PAUSE,
    PRIMARY_ACTION,
    SECONDARY_ACTION,
    JUMP,
    MOVE_LEFT,
    MOVE_RIGHT,
    MOVE_UP,
    MOVE_DOWN,
    HORIZONTAL,
    VERTICAL,
    MOVEMENT
}

enum class ControllerOwner {
    DEBUG,
    PLAYER,
    MENU
}

fun createUiGameInputSignals() =
    SceneGraph.UiInputSignals(
        GameInput.UI_ACCEPT,
        GameInput.UI_SELECT,
        GameInput.UI_CANCEL,
        GameInput.UI_FOCUS_NEXT,
        GameInput.UI_FOCUS_PREV,
        GameInput.UI_LEFT,
        GameInput.UI_RIGHT,
        GameInput.UI_UP,
        GameInput.UI_DOWN,
        GameInput.UI_HOME,
        GameInput.UI_END
    )

fun InputMapController<GameInput>.setupController() {
    val isQwerty = Config.keyboardType == Config.KeyboardType.QWERTY
    addBinding(
        GameInput.JUMP,
        listOf(Key.SPACE, Key.W, Key.ARROW_UP),
        buttons = listOf(GameButton.XBOX_A)
    )
    addBinding(
        GameInput.MOVE_LEFT,
        listOf(if (isQwerty) Key.A else Key.Q, Key.ARROW_LEFT),
        axes = listOf(GameAxis.LX)
    )
    addBinding(GameInput.MOVE_RIGHT, listOf(Key.D, Key.ARROW_RIGHT), axes = listOf(GameAxis.LX))
    addBinding(
        GameInput.MOVE_UP,
        listOf(if (isQwerty) Key.W else Key.Z, Key.ARROW_UP),
        axes = listOf(GameAxis.LY)
    )
    addBinding(GameInput.MOVE_DOWN, listOf(Key.S, Key.ARROW_DOWN), axes = listOf(GameAxis.LY))

    addBinding(
        GameInput.PRIMARY_ACTION,
        buttons = listOf(GameButton.XBOX_X),
        pointers = listOf(Pointer.MOUSE_LEFT)
    )

    addAxis(
        type = GameInput.HORIZONTAL,
        positive = GameInput.MOVE_RIGHT,
        negative = GameInput.MOVE_LEFT
    )
    addAxis(type = GameInput.VERTICAL, positive = GameInput.MOVE_UP, negative = GameInput.MOVE_DOWN)

    addVector(
        type = GameInput.MOVEMENT,
        positiveX = GameInput.MOVE_RIGHT,
        positiveY = GameInput.MOVE_UP,
        negativeX = GameInput.MOVE_LEFT,
        negativeY = GameInput.MOVE_DOWN
    )

    addBinding(GameInput.PAUSE, keys = listOf(Key.ESCAPE), buttons = listOf(GameButton.START))
}
