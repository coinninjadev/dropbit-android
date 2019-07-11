package com.coinninja.coinkeeper.util.image

import android.graphics.Bitmap
import android.graphics.Color
import app.dropbit.annotations.Mockable

import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter

import java.io.ByteArrayOutputStream
import java.io.IOException

@Mockable
class QRGeneratorUtil(private val qrCodeWriter: QRCodeWriter) {

    @Throws(WriterException::class)
    fun generateFrom(data: String): ByteArray {
        val bitmap = toBitmap(qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT))
        val byteStream = ByteArrayOutputStream()
        compress(bitmap, byteStream)

        val bytes = byteStream.toByteArray()

        try {
            byteStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return bytes
    }

    private fun compress(bitmap: Bitmap, bytes: ByteArrayOutputStream) {
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bytes)
    }

    private fun toBitmap(bitMatrix: BitMatrix): Bitmap {
        val height = bitMatrix.height
        val width = bitMatrix.width
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    companion object {
        private val QR_WIDTH = 300
        private val QR_HEIGHT = 300
    }
}
