package com.lehaine.game.component

import com.github.quillraven.fleks.EntityTags
import com.github.quillraven.fleks.entityTagOf

enum class RenderLayer : EntityTags by entityTagOf() {
    BACKGROUND, MAIN, FOREGROUND
}