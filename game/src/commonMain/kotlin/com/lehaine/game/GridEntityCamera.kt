package com.lehaine.game

import com.lehaine.littlekt.extras.Cooldown
import com.lehaine.littlekt.extras.ecs.component.GridComponent
import com.lehaine.littlekt.extras.graphics.PixelSmoothCamera
import com.lehaine.littlekt.math.MutableVec2f
import com.lehaine.littlekt.math.Rect
import com.lehaine.littlekt.math.clamp
import com.lehaine.littlekt.math.floor
import com.lehaine.littlekt.math.geom.Angle
import com.lehaine.littlekt.math.geom.cosine
import com.lehaine.littlekt.math.geom.radians
import com.lehaine.littlekt.math.geom.sine
import kotlin.math.*
import kotlin.time.Duration

/**
 * @author Colton Daily
 * @date 3/1/2023
 */
class GridEntityCamera : PixelSmoothCamera() {
    var tmod: Float = 1f

    /**
     * The view bounds of this camera in 1:1 pixels unit. The [ppu] is taken into account in [trueViewBounds].
     */
    val viewBounds: Rect = Rect()
    var clampToBounds = true
    var brakeDistanceNearBounds = 0.1f
    var following: GridComponent? = null
        private set

    var deadZonePctX = 0.04f
    var deadZonePctY = 0.1f

    var friction = 0.89f
    var bumpFrict = 0.85f
    var trackingSpeed = 1f

    var shakePower = 1f
    var shakeFrames = 0
    var dx = 0f
    var dy = 0f
    var dz = 0f
    var bumpX = 0f
    var bumpY = 0f
    var bumpZoomFactor = 0f
    var targetZoom = 1f
    var zoomSpeed = 0.0014f
    var zoomFrict = 0.9f

    val combinedZoom get() = zoom + bumpZoomFactor

    private val trueViewBounds
        get() = _trueViewBounds.set(
            0f,
            0f,
            viewBounds.width * ppuInv,
            viewBounds.height * ppuInv
        )
    private val _trueViewBounds = Rect()
    private val width get() = virtualWidth * combinedZoom
    private val height get() = virtualHeight * combinedZoom

    private val rawFocus = MutableVec2f()
    private val clampedFocus = MutableVec2f()

    private val cd = Cooldown()

    var scaledDistX: Float = 0f
        private set
    var scaledDistY: Float = 0f
        private set


    fun update(dt: Duration) {
        cd.update(dt)
        updatePosition()
        sync()
    }

    fun updatePosition() {
        val tz = targetZoom
        if (tz != zoom) {
            if (tz > zoom) {
                dz += zoomSpeed
            } else {
                dz -= zoomSpeed
            }
        } else {
            dz = 0f
        }
        val prevZoom = zoom
        zoom += dz * tmod
        bumpZoomFactor *= (0.9f).pow(tmod)
        dz *= zoomFrict.pow(tmod)
        if (abs(tz - zoom) <= 0.05f * tmod) {
            dz *= (0.8f).pow(tmod)
        }

        if (prevZoom < tz && zoom >= tz || prevZoom > tz && zoom <= tz) {
            zoom = tz
            dz = 0f
        }

        val following = following
        if (following != null) {
            val tx = following.centerX.floor()
            val ty = following.centerY.floor()
            val angle = atan2(ty - rawFocus.y, tx - rawFocus.x).radians
            val distX = abs(tx - rawFocus.x)
            if (distX >= deadZonePctX * width) {
                val speedX = 0.015f * combinedZoom * trackingSpeed
                dx += angle.cosine * (0.8f * distX - deadZonePctX * width) * speedX * tmod
            }
            val distY = abs(ty - rawFocus.y)
            if (distY >= deadZonePctY * height) {
                val speedY = 0.023f * combinedZoom * trackingSpeed
                dy += angle.sine * (0.8f * distY - deadZonePctY * height) * speedY * tmod
            }
        }

        var frictX = friction - trackingSpeed * combinedZoom * 0.054f * friction
        var frictY = frictX

        if (clampToBounds) {
            val brakeDistX = brakeDistanceNearBounds * width
            if (dx <= 0) {
                val brakeRatio = 1 - ((rawFocus.x - width * 0.5f) / brakeDistX).clamp(0f, 1f)
                frictX *= 1 - 0.9f * brakeRatio
            } else if (dx > 0) {
                val brakeRatio =
                    1 - (((trueViewBounds.width - width * 0.5f) - rawFocus.x) / brakeDistX).clamp(0f, 1f)
                frictX *= 1 - 0.9f * brakeRatio
            }

            val brakeDistY = brakeDistanceNearBounds * height
            if (dy < 0) {
                val brakeRatio = 1 - ((rawFocus.y - height * 0.5f) / brakeDistY).clamp(0f, 1f)
                frictY *= 1 - 0.9f * brakeRatio
            } else if (dy > 0) {
                val brakeRatio =
                    1 - (((trueViewBounds.height - height * 0.5f) - rawFocus.y) / brakeDistY).clamp(0f, 1f)
                frictY *= 1 - 0.9f * brakeRatio
            }
        }

        rawFocus.x += dx * tmod
        rawFocus.y += dy * tmod
        dx *= frictX.pow(tmod)
        dy *= frictY.pow(tmod)

        bumpX *= bumpFrict.pow(tmod)
        bumpY *= bumpFrict.pow(tmod)

        if (clampToBounds) {
            clampedFocus.x = if (trueViewBounds.width < width - offset.x * combinedZoom) {
                trueViewBounds.width * 0.5f
            } else {
                rawFocus.x.clamp(
                    width * 0.5f - offset.x * combinedZoom,
                    trueViewBounds.width - width * 0.5f + offset.x * combinedZoom
                )
            }

            clampedFocus.y = if (trueViewBounds.height < height - offset.y * combinedZoom) {
                trueViewBounds.height * 0.5f
            } else {
                rawFocus.y.clamp(
                    height * 0.5f - offset.y * combinedZoom,
                    trueViewBounds.height - height * 0.5f + offset.y * combinedZoom
                )
            }
        } else {
            clampedFocus.x = rawFocus.x
            clampedFocus.y = rawFocus.y
        }
    }

    private fun sync() {
        var targetX = clampedFocus.x
        var targetY = clampedFocus.y
        if (cd.has(SHAKE)) {
            targetX += cos(shakeFrames * 1.1f) * 2.5f * shakePower * cd.ratio(SHAKE)
            targetY += sin(0.3f + shakeFrames * 1.7f) * 2.5f * shakePower * cd.ratio(SHAKE)
            shakeFrames++
        } else {
            shakeFrames = 0
        }

        val tx = (targetX * ppu).floor() / ppu
        val ty = (targetY * ppu).floor() / ppu
        scaledDistX = (targetX - tx) * ppu
        scaledDistY = (targetY - ty) * ppu

        position.x = tx + offset.x * combinedZoom
        position.y = ty + offset.y * combinedZoom
    }

    fun shake(time: Duration, power: Float = 1f) {
        cd.timeout(SHAKE, time)
        shakePower = power
    }

    fun bump(x: Float = 0f, y: Float = 0f) {
        bumpX += x
        bumpY += y
    }

    fun bump(x: Int = 0, y: Int = 0) = bump(x.toFloat(), y.toFloat())

    fun bump(angle: Angle, distance: Int) {
        bumpX += angle.cosine * distance
        bumpY += angle.radians * distance
    }

    fun follow(gridCmp: GridComponent?, setImmediately: Boolean = false) {
        following = gridCmp
        if (setImmediately) {
            gridCmp ?: error("Target entity not set!!")
            rawFocus.x = gridCmp.centerX
            rawFocus.y = gridCmp.centerY
        }
    }

    fun unfollow() {
        following = null
    }

    companion object {
        private const val SHAKE = "shake"
    }
}