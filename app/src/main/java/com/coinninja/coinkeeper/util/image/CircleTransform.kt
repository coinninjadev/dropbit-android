package com.coinninja.coinkeeper.util.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import app.dropbit.annotations.Mockable
import com.squareup.picasso.Transformation
import javax.inject.Inject

/**
 * Reference Example: https://gist.github.com/aprock/6213395
 */
@Mockable
class CircleTransform @Inject constructor(val bitmapUtil: BitmapUtil) : Transformation {
    override fun key(): String {
        return "circle-transform-$accentColor"
    }

    var accentColor: Int = 0
    val strokeWidth = 2F

    override fun transform(source: Bitmap): Bitmap {
        if (source.isRecycled) return source

        val size = Math.min(source.width, source.height)
        val croppedBitmap = cropFromCenter(source, size, size)
        if (croppedBitmap != source) {
            source.recycle()
        }
        val transformedBitmap = bitmapUtil.createEmptyBitmap(size, size, source.config)

        val radius = size / 2F
        val canvas = bitmapUtil.createEmptyCanvasFrom(transformedBitmap)
        val paint = bitmapUtil.createPaint()
        paint.shader = bitmapUtil.createClampedShaderFor(croppedBitmap)
        paint.isDither = true

        addAccent(canvas, radius, radius, radius)

        val fillRadius =
                if (accentColor != 0) {
                    radius - strokeWidth * 2
                } else {
                    radius
                }

        canvas.drawCircle(radius, radius, fillRadius, paint)
        croppedBitmap.recycle()
        return transformedBitmap
    }

    private fun addAccent(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        if (accentColor == 0) return
        val paint = bitmapUtil.createPaint()
        paint.apply {
            style = Paint.Style.FILL
            isDither = true
            color = accentColor
        }

        canvas.drawCircle(cx, cy, radius, paint)
    }

    internal fun cropFromCenter(source: Bitmap, width: Int, height: Int): Bitmap {
        val X = (source.width - width) / 2
        val Y = (source.height - height) / 2
        return bitmapUtil.createBitmapFrom(source, X, Y, width, height)
    }
}