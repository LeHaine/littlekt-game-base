package com.lehaine.game.util

import kotlin.random.Random

val Float.randomSign: Float
    get() = if (Random.nextBoolean()) this else -this

val Int.randomSign: Int
    get() = if (Random.nextBoolean()) this else -this