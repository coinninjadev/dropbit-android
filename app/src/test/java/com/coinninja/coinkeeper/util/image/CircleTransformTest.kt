package com.coinninja.coinkeeper.util.image

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertTrue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class CircleTransformTest {


    @Test
    fun `creates empty canvas from bitmaps specs`() {
        val width = 100
        val height = 110
        val source = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val croppedSource = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val circleTransform = CircleTransform(mock(BitmapUtil::class.java))
        val canvas = mock(Canvas::class.java)
        val shader = mock(BitmapShader::class.java)
        val paint = mock(Paint::class.java)
        val circledImage = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

        whenever(circleTransform.bitmapUtil.createBitmapFrom(source, 0, 5, 100, 100))
                .thenReturn(croppedSource)
        whenever(circleTransform.bitmapUtil.createEmptyBitmap(100, 100, croppedSource.config)).thenReturn(circledImage)
        whenever(circleTransform.bitmapUtil.createEmptyCanvasFrom(circledImage)).thenReturn(canvas)
        whenever(circleTransform.bitmapUtil.createClampedShaderFor(croppedSource)).thenReturn(shader)
        whenever(circleTransform.bitmapUtil.createPaint()).thenReturn(paint)

        val radius = 50F


        assertThat(circleTransform.transform(source), equalTo(circledImage))
        verify(canvas).drawCircle(radius, radius, radius, paint)
        verify(paint).shader = shader
        verify(paint).isDither = true
        assertTrue(croppedSource.isRecycled)
        assertTrue(source.isRecycled)

        circledImage.recycle()

    }
}