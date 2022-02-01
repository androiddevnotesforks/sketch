package com.github.panpf.sketch.test.decode

import android.graphics.Bitmap
import android.graphics.Bitmap.Config.RGB_565
import androidx.test.runner.AndroidJUnit4
import com.github.panpf.sketch.decode.BitmapDecodeResult
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.sketch.decode.Resize.Scale.CENTER_CROP
import com.github.panpf.sketch.decode.internal.InSampledTransformed
import com.github.panpf.sketch.decode.transform.CircleCropTransformed
import com.github.panpf.sketch.decode.transform.RotateTransformed
import com.github.panpf.sketch.request.DataFrom.LOCAL
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BitmapDecodeResultTest {

    @Test
    fun testConstructor() {
        val newBitmap = Bitmap.createBitmap(100, 100, RGB_565)
        val imageInfo = ImageInfo("image/png", 3000, 500, 0)
        val transformedList = listOf(InSampledTransformed(4), RotateTransformed(45))
        BitmapDecodeResult(newBitmap, imageInfo, LOCAL, transformedList).apply {
            Assert.assertTrue(newBitmap === bitmap)
            Assert.assertEquals(
                "ImageInfo(mimeType='image/png',width=3000,height=500,exifOrientation=UNDEFINED)",
                imageInfo.toString()
            )
            Assert.assertEquals(LOCAL, dataFrom)
            Assert.assertEquals(
                "InSampledTransformed(4), RotateTransformed(45)",
                this.transformedList?.joinToString()
            )
        }
    }

    @Test
    fun testNew() {
        val newBitmap = Bitmap.createBitmap(100, 100, RGB_565)
        val imageInfo = ImageInfo("image/png", 3000, 500, 0)
        val transformedList = listOf(InSampledTransformed(4), RotateTransformed(45))
        val result = BitmapDecodeResult(newBitmap, imageInfo, LOCAL, transformedList)
        Assert.assertEquals(
            "InSampledTransformed(4), RotateTransformed(45)",
            result.transformedList?.joinToString()
        )

        val result2 = result.new(newBitmap){
            addTransformed(CircleCropTransformed(CENTER_CROP))
        }
        Assert.assertEquals(
            "InSampledTransformed(4), RotateTransformed(45)",
            result.transformedList?.joinToString()
        )
        Assert.assertEquals(
            "InSampledTransformed(4), RotateTransformed(45), CircleCropTransformed(CENTER_CROP)",
            result2.transformedList?.joinToString()
        )
    }
}