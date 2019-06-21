package com.coinninja.coinkeeper.util.image

import android.graphics.*
import app.dropbit.annotations.Mockable
import javax.inject.Inject

@Mockable
class BitmapUtil @Inject constructor() {

    fun createBitmapFrom(source: Bitmap, x: Int, y: Int, width: Int, height: Int): Bitmap {
        return Bitmap.createBitmap(source, x, y, width, height)
    }

    fun createEmptyBitmap(width: Int, height: Int, config: Bitmap.Config): Bitmap {
        return Bitmap.createBitmap(width, height, config)
    }

    fun createEmptyCanvasFrom(bitmap: Bitmap): Canvas {
        return Canvas(bitmap)
    }

    fun createClampedShaderFor(bitmap: Bitmap): BitmapShader {
        return BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }

    fun createPaint(): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG)
    }

}