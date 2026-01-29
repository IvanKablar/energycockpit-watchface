package com.current.wearos.energycockpit

import android.content.ComponentName
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.view.SurfaceHolder
import androidx.wear.watchface.CanvasComplication
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.ComplicationSlot
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchFace
import androidx.wear.watchface.WatchFaceService
import androidx.wear.watchface.WatchFaceType
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.complications.ComplicationSlotBounds
import androidx.wear.watchface.complications.DefaultComplicationDataSourcePolicy
import androidx.wear.watchface.complications.SystemDataSources
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.rendering.CanvasComplicationDrawable
import androidx.wear.watchface.complications.rendering.ComplicationDrawable
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyleSchema
import java.time.ZonedDateTime

class EnergyCockpitWatchFaceService : WatchFaceService() {

    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        val renderer = TestWatchFaceRenderer(
            surfaceHolder = surfaceHolder,
            watchState = watchState,
            complicationSlotsManager = complicationSlotsManager,
            currentUserStyleRepository = currentUserStyleRepository,
            context = this
        )

        return WatchFace(
            watchFaceType = WatchFaceType.DIGITAL,
            renderer = renderer
        )
    }

    override fun createComplicationSlotsManager(
        currentUserStyleRepository: CurrentUserStyleRepository
    ): ComplicationSlotsManager {
        // Standard factory for most complications
        val standardFactory: (WatchState, CanvasComplication.InvalidateCallback) -> CanvasComplication = { watchState, listener ->
            CanvasComplicationDrawable(
                ComplicationDrawable(this),
                watchState,
                listener
            )
        }

        // ColorRamp factory for RANGED_VALUE complications
        val colorRampFactory: (WatchState, CanvasComplication.InvalidateCallback) -> CanvasComplication = { watchState, listener ->
            ColorRampCanvasComplication(
                ComplicationDrawable(this),
                watchState,
                listener
            )
        }

        // Tibber Complication ComponentName
        val tibberComplication = ComponentName(
            "com.current.wearos.free.debug",
            "com.current.wearos.complication.PriceComplicationService"
        )

        // Top Left - SHORT TEXT (for Tibber price)
        val shortTextSlot = ComplicationSlot.createRoundRectComplicationSlotBuilder(
            id = 100,
            canvasComplicationFactory = standardFactory,
            supportedTypes = listOf(
                ComplicationType.SHORT_TEXT,
                ComplicationType.RANGED_VALUE,
                ComplicationType.MONOCHROMATIC_IMAGE,
                ComplicationType.SMALL_IMAGE
            ),
            defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
                tibberComplication,
                SystemDataSources.DATA_SOURCE_DAY_OF_WEEK
            ),
            bounds = ComplicationSlotBounds(
                RectF(0.15f, 0.25f, 0.45f, 0.38f)
            )
        ).build()

        // Top Right - LONG TEXT (for Tibber price)
        val longTextSlot = ComplicationSlot.createRoundRectComplicationSlotBuilder(
            id = 101,
            canvasComplicationFactory = standardFactory,
            supportedTypes = listOf(
                ComplicationType.LONG_TEXT,
                ComplicationType.SHORT_TEXT,
                ComplicationType.RANGED_VALUE
            ),
            defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
                tibberComplication,
                SystemDataSources.DATA_SOURCE_DATE
            ),
            bounds = ComplicationSlotBounds(
                RectF(0.55f, 0.25f, 0.85f, 0.38f)
            )
        ).build()

        // Bottom Left - RANGED VALUE with ColorRamp support (for Tibber price)
        val rangedValueSlot = ComplicationSlot.createRoundRectComplicationSlotBuilder(
            id = 102,
            canvasComplicationFactory = colorRampFactory,
            supportedTypes = listOf(
                ComplicationType.RANGED_VALUE,
                ComplicationType.SHORT_TEXT,
                ComplicationType.LONG_TEXT
            ),
            defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
                tibberComplication,
                SystemDataSources.DATA_SOURCE_STEP_COUNT
            ),
            bounds = ComplicationSlotBounds(
                RectF(0.15f, 0.62f, 0.45f, 0.75f)
            )
        ).build()

        // Bottom Right - IMAGE (for custom complications)
        val monochromaticImageSlot = ComplicationSlot.createRoundRectComplicationSlotBuilder(
            id = 103,
            canvasComplicationFactory = standardFactory,
            supportedTypes = listOf(
                ComplicationType.MONOCHROMATIC_IMAGE,
                ComplicationType.SHORT_TEXT,
                ComplicationType.SMALL_IMAGE
            ),
            defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
                SystemDataSources.DATA_SOURCE_SUNRISE_SUNSET,
                ComplicationType.MONOCHROMATIC_IMAGE
            ),
            bounds = ComplicationSlotBounds(
                RectF(0.55f, 0.62f, 0.85f, 0.75f)
            )
        ).build()

        return ComplicationSlotsManager(
            listOf(shortTextSlot, longTextSlot, rangedValueSlot, monochromaticImageSlot),
            currentUserStyleRepository
        )
    }

    override fun createUserStyleSchema(): UserStyleSchema {
        return UserStyleSchema(emptyList())
    }

    private class TestWatchFaceRenderer(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        private val complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository,
        private val context: Context
    ) : Renderer.CanvasRenderer2<TestWatchFaceRenderer.TestSharedAssets>(
        surfaceHolder = surfaceHolder,
        currentUserStyleRepository = currentUserStyleRepository,
        watchState = watchState,
        canvasType = CanvasType.HARDWARE,
        interactiveDrawModeUpdateDelayMillis = 16L,
        clearWithBackgroundTintBeforeRenderingHighlightLayer = false
    ) {

        class TestSharedAssets : SharedAssets {
            override fun onDestroy() {}
        }

        // Tibber brand color
        private val tibberBlue = Color.parseColor("#00A9E0")

        private val backgroundPaint = Paint().apply {
            color = Color.BLACK
        }

        private val timePaint = Paint().apply {
            color = Color.WHITE
            textSize = 80f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        }

        private val datePaint = Paint().apply {
            color = tibberBlue
            textSize = 24f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        private val labelPaint = Paint().apply {
            color = Color.GRAY
            textSize = 16f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        private val accentPaint = Paint().apply {
            color = tibberBlue
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }

        override suspend fun createSharedAssets(): TestSharedAssets {
            return TestSharedAssets()
        }

        override fun render(
            canvas: Canvas,
            bounds: Rect,
            zonedDateTime: ZonedDateTime,
            sharedAssets: TestSharedAssets
        ) {
            // Schwarzer Hintergrund
            canvas.drawRect(bounds, backgroundPaint)

            val centerX = bounds.exactCenterX()
            val centerY = bounds.exactCenterY()

            // Zeichne dezenten Kreis als Design-Element
            canvas.drawCircle(centerX, centerY, centerX * 0.85f, accentPaint)

            // Uhrzeit in der Mitte
            val timeText = String.format(
                "%02d:%02d",
                zonedDateTime.hour,
                zonedDateTime.minute
            )
            canvas.drawText(timeText, centerX, centerY + 20f, timePaint)

            // Datum unter der Uhrzeit
            val dayOfWeek = when (zonedDateTime.dayOfWeek.value) {
                1 -> "MON"
                2 -> "TUE"
                3 -> "WED"
                4 -> "THU"
                5 -> "FRI"
                6 -> "SAT"
                7 -> "SUN"
                else -> ""
            }
            val dateText = String.format(
                "%s, %02d.%02d",
                dayOfWeek,
                zonedDateTime.dayOfMonth,
                zonedDateTime.monthValue
            )
            canvas.drawText(dateText, centerX, centerY + 55f, datePaint)

            // Zeichne Komplikationen
            for ((_, complication) in complicationSlotsManager.complicationSlots) {
                if (complication.enabled) {
                    complication.render(canvas, zonedDateTime, renderParameters)
                }
            }
        }

        override fun renderHighlightLayer(
            canvas: Canvas,
            bounds: Rect,
            zonedDateTime: ZonedDateTime,
            sharedAssets: TestSharedAssets
        ) {
            // Nicht benötigt für dieses simple Watchface
        }
    }
}
