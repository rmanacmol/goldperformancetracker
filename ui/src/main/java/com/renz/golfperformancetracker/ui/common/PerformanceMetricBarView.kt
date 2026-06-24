package com.renz.golfperformancetracker.ui.common

import android.R.attr.maxWidth
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.renz.golfperformancetracker.ui.R
import kotlin.math.min

/**
 * Lightweight custom view that visualizes normalized performance metrics as horizontal bars.
 */
class PerformanceMetricBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val speedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val launchPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val distancePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val barRect = RectF()
    private var speed = 0.0
    private var launchAngle = 0.0
    private var carryDistance = 0.0

    private val barHeight = resources.getDimension(R.dimen.metric_bar_height)
    private val barGap = resources.getDimension(R.dimen.metric_bar_gap)
    private val cornerRadius = resources.getDimension(R.dimen.metric_bar_corner_radius)

    init {
        speedPaint.color = ContextCompat.getColor(context, R.color.metric_speed)
        launchPaint.color = ContextCompat.getColor(context, R.color.metric_launch)
        distancePaint.color = ContextCompat.getColor(context, R.color.metric_distance)
        trackPaint.color = ContextCompat.getColor(context, R.color.metric_track)
    }

    fun setMetrics(speed: Double, launchAngle: Double, carryDistance: Double) {
        this.speed = speed
        this.launchAngle = launchAngle
        this.carryDistance = carryDistance
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = (barHeight * 3 + barGap * 2).toInt()
        val resolvedHeight = resolveSize(desiredHeight, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(resolvedHeight, MeasureSpec.EXACTLY))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val maxWidth = width.toFloat()
        drawBar(canvas, 0f, 1f, trackPaint)
        drawBar(canvas, 0f, normalized(speed, MAX_SPEED), speedPaint)
        drawBar(canvas, barHeight + barGap, 1f, trackPaint)
        drawBar(canvas, barHeight + barGap, normalized(launchAngle, MAX_LAUNCH), launchPaint)
        drawBar(canvas, (barHeight + barGap) * 2, 1f, trackPaint)
        drawBar(canvas, (barHeight + barGap) * 2, normalized(carryDistance, MAX_DISTANCE), distancePaint)
    }

    private fun drawBar(canvas: Canvas, topOffset: Float, fraction: Float, paint: Paint) {
        barRect.set(0f, topOffset, maxWidth * min(fraction, 1f), topOffset + barHeight)
        canvas.drawRoundRect(barRect, cornerRadius, cornerRadius, paint)
    }

    private fun normalized(value: Double, max: Double): Float =
        (value / max).toFloat().coerceIn(0.05f, 1f)

    companion object {
        private const val MAX_SPEED = 180.0
        private const val MAX_LAUNCH = 30.0
        private const val MAX_DISTANCE = 300.0
    }
}
