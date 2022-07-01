package com.github.panpf.sketch.test.transform

import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.sketch.fetch.newAssetUri
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.sketch
import com.github.panpf.sketch.test.utils.corners
import com.github.panpf.sketch.test.utils.size
import com.github.panpf.sketch.transform.MaskTransformation
import com.github.panpf.sketch.transform.MaskTransformed
import com.github.panpf.sketch.util.Size
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MaskTransformationTest {

    @Test
    fun testConstructor() {
        MaskTransformation(Color.BLACK).apply {
            Assert.assertEquals(Color.BLACK, maskColor)
        }
        MaskTransformation(Color.GREEN).apply {
            Assert.assertEquals(Color.GREEN, maskColor)
        }
    }

    @Test
    fun testKeyAndToString() {
        MaskTransformation(Color.BLACK).apply {
            Assert.assertEquals("MaskTransformation(${Color.BLACK})", key)
            Assert.assertEquals("MaskTransformation(${Color.BLACK})", toString())
        }
        MaskTransformation(Color.GREEN).apply {
            Assert.assertEquals("MaskTransformation(${Color.GREEN})", key)
            Assert.assertEquals("MaskTransformation(${Color.GREEN})", toString())
        }
    }

    @Test
    fun testTransform() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = context.sketch
        val request = DisplayRequest(context, newAssetUri("sample.jpeg"))
        val inBitmap = context.assets.open("sample.jpeg").use {
            BitmapFactory.decodeStream(it)
        }.apply {
            Assert.assertNotEquals(
                listOf(Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT),
                this.corners()
            )
            Assert.assertEquals(
                Size(1291, 1936),
                this.size
            )
            Assert.assertFalse(this.isMutable)
        }
        val inBitmapCorners = inBitmap.corners()

        val maskColor = ColorUtils.setAlphaComponent(Color.GREEN, 100)
        runBlocking {
            MaskTransformation(maskColor).transform(sketch, request, inBitmap)
        }.apply {
            Assert.assertNotSame(inBitmap, this)
            Assert.assertNotEquals(inBitmapCorners, bitmap.corners())
            Assert.assertEquals(Size(1291, 1936), bitmap.size)
            Assert.assertEquals(MaskTransformed(maskColor), transformed)
        }

        val mutableInBitmap = context.assets.open("sample.jpeg").use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply {
                inMutable = true
            })
        }!!.apply {
            Assert.assertTrue(this.isMutable)
        }

        runBlocking {
            MaskTransformation(maskColor).transform(sketch, request, mutableInBitmap)
        }.apply {
            Assert.assertSame(mutableInBitmap, this.bitmap)
        }
    }

    @Test
    fun testEqualsAndHashCode() {
        val element1 = MaskTransformation(Color.RED)
        val element11 = MaskTransformation(Color.RED)
        val element2 = MaskTransformation(Color.BLACK)

        Assert.assertNotSame(element1, element11)
        Assert.assertNotSame(element1, element2)
        Assert.assertNotSame(element2, element11)

        Assert.assertEquals(element1, element1)
        Assert.assertEquals(element1, element11)
        Assert.assertNotEquals(element1, element2)
        Assert.assertNotEquals(element2, element11)
        Assert.assertNotEquals(element1, null)
        Assert.assertNotEquals(element1, Any())

        Assert.assertEquals(element1.hashCode(), element1.hashCode())
        Assert.assertEquals(element1.hashCode(), element11.hashCode())
        Assert.assertNotEquals(element1.hashCode(), element2.hashCode())
        Assert.assertNotEquals(element2.hashCode(), element11.hashCode())
    }
}