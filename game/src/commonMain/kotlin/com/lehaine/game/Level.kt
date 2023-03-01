package com.lehaine.game

import com.lehaine.littlekt.extras.GameLevel
import com.lehaine.littlekt.graphics.g2d.tilemap.ldtk.LDtkIntGridLayer
import com.lehaine.littlekt.graphics.g2d.tilemap.ldtk.LDtkLevel
import com.lehaine.littlekt.math.clamp

class Level(ldtkLevel: LDtkLevel) : GameLevel<Level.LevelMark> {
    override var gridSize: Int = Config.GRID_CELL_SIZE

    private val marks = mutableMapOf<LevelMark, MutableMap<Int, Int>>()

    // a list of collision int values from LDtk world
    private val collisionValues = intArrayOf(1)
    private val collisionLayer = ldtkLevel["Collisions"] as LDtkIntGridLayer

    override fun isValid(cx: Int, cy: Int) = collisionLayer.isCoordValid(cx, cy)
    override fun getCoordId(cx: Int, cy: Int) = collisionLayer.getCoordId(cx, cy)

    override fun hasCollision(cx: Int, cy: Int): Boolean {
        return if (isValid(cx, cy)) {
            collisionValues.contains(collisionLayer.getInt(cx, cy))
        } else {
            true
        }
    }

    override fun hasMark(cx: Int, cy: Int, mark: LevelMark, dir: Int): Boolean {
        return marks[mark]?.get(getCoordId(cx, cy)) == dir && isValid(cx, cy)
    }

    override fun setMarks(cx: Int, cy: Int, marks: List<LevelMark>) {
        marks.forEach {
            setMark(cx, cy, it, 0)
        }
    }

    override fun setMark(cx: Int, cy: Int, mark: LevelMark, dir: Int) {
        if (isValid(cx, cy) && !hasMark(cx, cy, mark, dir)) {
            if (!marks.contains(mark)) {
                marks[mark] = mutableMapOf()
            }

            marks[mark]?.set(getCoordId(cx, cy), dir.clamp(-1, 1))
        }
    }

    // set level marks at start of level creation to react to certain tiles
    fun createLevelMarks() = Unit

    enum class LevelMark {

    }
}