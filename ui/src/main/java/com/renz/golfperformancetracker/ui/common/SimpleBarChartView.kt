package com.renz.golfperformancetracker.ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.renz.golfperformancetracker.domain.model.ShotMetricPoint
import com.renz.golfperformancetracker.ui.R
import kotlin.math.max

/**
 * Canvas bar chart for visualizing ball speed trends across shots.
 */
class SimpleBarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = resources.getDimension(R.dimen.chart_label_text_size)
        color = ContextCompat.getColor(context, R.color.golf_on_surface)
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.metric_track)
        strokeWidth = resources.getDimension(R.dimen.chart_grid_stroke)
    }

    private val barRect = RectF()
    private var points: List<ShotMetricPoint> = emptyList()
    private var maxValue: Double = 1.0

    init {
        barPaint.color = ContextCompat.getColor(context, R.color.metric_speed)
    }

    fun setData(points: List<ShotMetricPoint>) {
        this.points = points
        maxValue = max(points.maxOfOrNull { it.ballSpeed } ?: 1.0, 1.0)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = resources.getDimensionPixelSize(R.dimen.chart_default_height)
        val resolvedHeight = resolveSize(desiredHeight, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(resolvedHeight, MeasureSpec.EXACTLY))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (points.isEmpty()) return

        val chartTop = paddingTop.toFloat()
        val chartBottom = height - paddingBottom - labelPaint.textSize - 8f
        val chartHeight = chartBottom - chartTop
        val slotWidth = (width - paddingStart - paddingEnd).toFloat() / points.size
        val barWidth = slotWidth * 0.6f

        canvas.drawLine(
            paddingStart.toFloat(),
            chartBottom,
            (width - paddingEnd).toFloat(),
            chartBottom,
            gridPaint,
        )

        points.forEachIndexed { index, point ->
            val fraction = (point.ballSpeed / maxValue).toFloat().coerceIn(0.05f, 1f)
            val left = paddingStart + index * slotWidth + (slotWidth - barWidth) / 2f
            val top = chartBottom - chartHeight * fraction
            barRect.set(left, top, left + barWidth, chartBottom)
            canvas.drawRoundRect(barRect, 8f, 8f, barPaint)
            canvas.drawText(
                point.label,
                left,
                height - paddingBottom.toFloat(),
                labelPaint,
            )
        }
    }
}
