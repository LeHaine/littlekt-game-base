**LittleKt Game Base**

This is a game base that I use for my personal projects. It contains a bunch of placeholders for things like asset
management and scene handling as well as graphic related things like effects, a custom camera, level management, and the
likes.

It uses `littlekt-extras` repo directly as a submodule.

`git clone --recurse-submodules https://github.com/LeHaine/littlekt-game-base.git`

**Features**:

It makes heavy use of the ECS pattern using the awesome [Fleks](https://github.com/Quillraven/Fleks) library. Everything
regarding the game scene is split into components and
systems.

This contains its own render stage system for splitting up complex rendering while being able to handle when batching
happens. These are meant to be used within a single `RenderSystem` which then contains stages and sub-render-pipelines.