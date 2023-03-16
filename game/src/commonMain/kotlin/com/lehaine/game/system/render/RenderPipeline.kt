package com.lehaine.game.system.render

interface RenderPipeline : RenderStage {
    val stages: List<RenderStage>
}
