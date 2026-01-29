package com.current.wearos.energycockpit

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.wear.watchface.CanvasComplication
import androidx.wear.watchface.RenderParameters
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.NoDataComplicationData
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.rendering.CanvasComplicationDrawable
import androidx.wear.watchface.complications.rendering.ComplicationDrawable
import java.time.ZonedDateTime

/**
 * Custom CanvasComplication that supports ColorRamp for RANGED_VALUE complications.
 * Falls back to standard ComplicationDrawable for other types.
 */
class ColorRampCanvasComplication(
    private val drawable: ComplicationDrawable,
    watchState: WatchState,
    private val invalidateCallback: CanvasComplication.InvalidateCallback
) : CanvasComplication {

    private val fallbackRenderer = CanvasComplicationDrawable(drawable, watchState, invalidateCallback)

    private var currentData: ComplicationData? = null

    private val arcPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }

    private val backgroundArcPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        color = Color.DKGRAY
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 24f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val titlePaint = Paint().apply {
        color = Color.WHITE
        textSize = 18f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    override fun render(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        renderParameters: RenderParameters,
        slotId: Int
    ) {
        val data = currentData

        // Check if we have RangedValueComplicationData with ColorRamp
        if (data is RangedValueComplicationData) {
            renderRangedValueWithColorRamp(canvas, bounds, data)
        } else {
            // Fallback to standard rendering
            fallbackRenderer.render(canvas, bounds, zonedDateTime, renderParameters, slotId)
        }
    }

    private fun renderRangedValueWithColorRamp(
        canvas: Canvas,
        bounds: Rect,
        data: RangedValueComplicationData
    ) {
        val value = data.value
        val min = data.min
        val max = data.max
        val colorRamp = data.colorRamp

        // Calculate progress (0.0 to 1.0)
        val progress = if (max > min) {
            ((value - min) / (max - min)).coerceIn(0f, 1f)
        } else {
            0.5f
        }

        // Determine arc color
        val arcColor = if (colorRamp != null && colorRamp.colors.isNotEmpty()) {
            interpolateColor(colorRamp.colors, progress, colorRamp.interpolated)
        } else {
            // Default Tibber colors based on progress
            when {
                progress < 0.2f -> Color.parseColor("#00D68F")  // Green
                progress < 0.4f -> Color.parseColor("#5AEEB4")  // Light Green
                progress < 0.6f -> Color.parseColor("#1ED0E7")  // Cyan
                progress < 0.8f -> Color.parseColor("#FFB800")  // Yellow
                else -> Color.parseColor("#FF6B6B")             // Red
            }
        }

        arcPaint.color = arcColor

        // Calculate arc bounds with padding
        val padding = 4f
        val arcBounds = RectF(
            bounds.left + padding,
            bounds.top + padding,
            bounds.right - padding,
            bounds.bottom - padding
        )

        // Draw background arc (full circle)
        canvas.drawArc(arcBounds, 135f, 270f, false, backgroundArcPaint)

        // Draw progress arc
        val sweepAngle = 270f * progress
        canvas.drawArc(arcBounds, 135f, sweepAngle, false, arcPaint)

        // Draw text (price)
        val text = data.text?.getTextAt(android.content.res.Resources.getSystem(), java.time.Instant.now())?.toString() ?: ""
        val centerX = bounds.exactCenterX()
        val centerY = bounds.exactCenterY()

        canvas.drawText(text, centerX, centerY + 8f, textPaint)

        // Draw title (lightning bolt or similar)
        val title = data.title?.getTextAt(android.content.res.Resources.getSystem(), java.time.Instant.now())?.toString() ?: ""
        if (title.isNotEmpty()) {
            canvas.drawText(title, centerX, centerY - 16f, titlePaint)
        }
    }

    /**
     * Interpolate color from ColorRamp based on progress
     */
    private fun interpolateColor(colors: IntArray, progress: Float, interpolated: Boolean): Int {
        if (colors.isEmpty()) return Color.GRAY
        if (colors.size == 1) return colors[0]

        if (!interpolated) {
            // Step colors - no interpolation
            val index = (progress * colors.size).toInt().coerceIn(0, colors.lastIndex)
            return colors[index]
        }

        // Interpolated colors
        val scaledProgress = progress * (colors.size - 1)
        val index = scaledProgress.toInt().coerceIn(0, colors.lastIndex - 1)
        val fraction = scaledProgress - index

        val startColor = colors[index]
        val endColor = colors[(index + 1).coerceAtMost(colors.lastIndex)]

        return blendColors(startColor, endColor, fraction)
    }

    /**
     * Blend two colors based on ratio (0.0 = startColor, 1.0 = endColor)
     */
    private fun blendColors(startColor: Int, endColor: Int, ratio: Float): Int {
        val inverseRatio = 1f - ratio

        val a = (Color.alpha(startColor) * inverseRatio + Color.alpha(endColor) * ratio).toInt()
        val r = (Color.red(startColor) * inverseRatio + Color.red(endColor) * ratio).toInt()
        val g = (Color.green(startColor) * inverseRatio + Color.green(endColor) * ratio).toInt()
        val b = (Color.blue(startColor) * inverseRatio + Color.blue(endColor) * ratio).toInt()

        return Color.argb(a, r, g, b)
    }

    override fun drawHighlight(
        canvas: Canvas,
        bounds: Rect,
        boundsType: Int,
        zonedDateTime: ZonedDateTime,
        color: Int
    ) {
        fallbackRenderer.drawHighlight(canvas, bounds, boundsType, zonedDateTime, color)
    }

    override fun getData(): ComplicationData = currentData ?: NoDataComplicationData()

    override fun loadData(
        complicationData: ComplicationData,
        loadDrawablesAsynchronous: Boolean
    ) {
        currentData = complicationData
        fallbackRenderer.loadData(complicationData, loadDrawablesAsynchronous)
        invalidateCallback.onInvalidate()
    }
}
