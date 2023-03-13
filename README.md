**LittleKt Game Base**

This is a game base that I use for my personal projects. It contains a bunch of placeholders for things like asset
managment and scene handling as well as graphic related things like effects, a custom camera, level management, and the
likes.

It uses `littlekt-extras` repo directly as a submodule.

`git clone --recurse-submodules https://github.com/LeHaine/littlekt-game-base.git`

**Branches**

* `master`: For the OOP structured way of building a game. Relies on extending the base `GridEntity` and building off of
  it.

* `fleks`: The ECS way. Similar grid structure as `master` branch except everything is split into components and
  systems.