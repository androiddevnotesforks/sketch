package com.github.panpf.sketch.test.decode.internal

import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.widget.ImageView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.panpf.sketch.cache.CachePolicy.DISABLED
import com.github.panpf.sketch.cache.CachePolicy.ENABLED
import com.github.panpf.sketch.cache.CachePolicy.READ_ONLY
import com.github.panpf.sketch.cache.CachePolicy.WRITE_ONLY
import com.github.panpf.sketch.datasource.DataFrom.LOCAL
import com.github.panpf.sketch.decode.BitmapDecodeResult
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.sketch.decode.internal.newBitmapMemoryCacheHelper
import com.github.panpf.sketch.fetch.newAssetUri
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.RequestDepth
import com.github.panpf.sketch.test.contextAndSketch
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BitmapMemoryCacheHelperTest {

    @Test
    fun testNewBitmapMemoryCacheHelper() {
        val (context, _) = contextAndSketch()
        val imageView = ImageView(context)
        val request = DisplayRequest(newAssetUri("sample.jpeg"), imageView)

        Assert.assertNotNull(
            newBitmapMemoryCacheHelper(request)
        )
        Assert.assertNotNull(
            newBitmapMemoryCacheHelper(request.newDisplayRequest {
                bitmapMemoryCachePolicy(ENABLED)
            })
        )
        Assert.assertNull(
            newBitmapMemoryCacheHelper(request.newDisplayRequest {
                bitmapMemoryCachePolicy(DISABLED)
            })
        )
        Assert.assertNotNull(
            newBitmapMemoryCacheHelper(request.newDisplayRequest {
                bitmapMemoryCachePolicy(READ_ONLY)
            })
        )
        Assert.assertNotNull(
            newBitmapMemoryCacheHelper(request.newDisplayRequest {
                bitmapMemoryCachePolicy(WRITE_ONLY)
            })
        )
    }

    @Test
    fun testRead() {
        val (context, sketch) = contextAndSketch()
        val imageView = ImageView(context)
        val request = DisplayRequest(newAssetUri("sample.jpeg"), imageView)

        sketch.memoryCache.clear()

        // Is there really no
        val helper = newBitmapMemoryCacheHelper(request)!!
        Assert.assertNull(helper.read())

        Assert.assertNull(
            newBitmapMemoryCacheHelper(request.newDisplayRequest {
                depth(RequestDepth.LOCAL)
            })!!.read()
        )

        // There are the
        val bitmapDecodeResult = BitmapDecodeResult(
            Bitmap.createBitmap(100, 100, ARGB_8888),
            ImageInfo(1291, 1936, "image/jpeg"),
            0,
            LOCAL,
            null
        )
        helper.write(bitmapDecodeResult)
        Assert.assertNotNull(helper.read())
        Assert.assertNotNull(helper.read())

        Assert.assertNotNull(
            newBitmapMemoryCacheHelper(request.newDisplayRequest {
                bitmapMemoryCachePolicy(ENABLED)
            })!!.read()
        )
        Assert.assertNotNull(
            newBitmapMemoryCacheHelper(request.newDisplayRequest {
                bitmapMemoryCachePolicy(READ_ONLY)
            })!!.read()
        )
        Assert.assertNull(
            newBitmapMemoryCacheHelper(request.newDisplayRequest {
                bitmapMemoryCachePolicy(WRITE_ONLY)
            })!!.read()
        )
    }

    @Test
    fun testWrite() {
        val (context, sketch) = contextAndSketch()
        val imageView = ImageView(context)
        val request = DisplayRequest(newAssetUri("sample.jpeg"), imageView)

        sketch.memoryCache.clear()

        Assert.assertNull(newBitmapMemoryCacheHelper(request)!!.read())

        val bitmapDecodeResult = BitmapDecodeResult(
            Bitmap.createBitmap(100, 100, ARGB_8888),
            ImageInfo(1291, 1936, "image/jpeg"),
            0,
            LOCAL,
            null
        )
        Assert.assertNotNull(
            newBitmapMemoryCacheHelper(request)!!.write(bitmapDecodeResult)
        )

        Assert.assertNotNull(newBitmapMemoryCacheHelper(request)!!.read())

        Assert.assertNotNull(
            newBitmapMemoryCacheHelper(request.newDisplayRequest {
                bitmapMemoryCachePolicy(ENABLED)
            })!!.write(bitmapDecodeResult)
        )
        Assert.assertNull(
            newBitmapMemoryCacheHelper(request.newDisplayRequest {
                bitmapMemoryCachePolicy(READ_ONLY)
            })!!.write(bitmapDecodeResult)
        )
        Assert.assertNotNull(
            newBitmapMemoryCacheHelper(request.newDisplayRequest {
                bitmapMemoryCachePolicy(WRITE_ONLY)
            })!!.write(bitmapDecodeResult)
        )

        Assert.assertNotNull(newBitmapMemoryCacheHelper(request)!!.read())
    }
}