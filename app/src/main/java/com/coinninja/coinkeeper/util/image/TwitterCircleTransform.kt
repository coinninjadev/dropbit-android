package com.coinninja.coinkeeper.util.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import androidx.core.content.res.ResourcesCompat
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext
import com.squareup.picasso.Transformation
import javax.inject.Inject

/**
 * Reference Example: https://gist.github.com/aprock/6213395
 */
@Mockable
class TwitterCircleTransform @Inject constructor(
        @ApplicationContext val context: Context,
        val bitmapUtil: BitmapUtil
) : Transformation {

    override fun key(): String {
        return "circle-transform-twitter-icon"
    }

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

        canvas.drawCircle(radius, radius, radius - 4, paint)
        addTwitterIcon(canvas, radius, radius, radius)

        croppedBitmap.recycle()
        return transformedBitmap
    }

    private fun addTwitterIcon(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        ResourcesCompat.getDrawable(context.resources, R.drawable.ic_twitter_profile_overlay, context.theme)?.let {
            it.bounds = Rect(0, canvas.height - 16, 16, canvas.height)
            it.draw(canvas)
        }
    }

    internal fun cropFromCenter(source: Bitmap, width: Int, height: Int): Bitmap {
        val X = (source.width - width) / 2
        val Y = (source.height - height) / 2
        return bitmapUtil.createBitmapFrom(source, X, Y, width, height)
    }
}