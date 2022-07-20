package com.github.panpf.sketch.test.request.internal

import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.drawable.BitmapDrawable
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.panpf.sketch.cache.CachePolicy.DISABLED
import com.github.panpf.sketch.cache.CachePolicy.ENABLED
import com.github.panpf.sketch.cache.CachePolicy.READ_ONLY
import com.github.panpf.sketch.cache.CachePolicy.WRITE_ONLY
import com.github.panpf.sketch.cache.CountBitmap
import com.github.panpf.sketch.datasource.DataFrom
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.sketch.drawable.SketchCountBitmapDrawable
import com.github.panpf.sketch.request.Depth.MEMORY
import com.github.panpf.sketch.request.DepthException
import com.github.panpf.sketch.request.DisplayData
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.DownloadData
import com.github.panpf.sketch.request.DownloadRequest
import com.github.panpf.sketch.request.ImageData
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.LoadData
import com.github.panpf.sketch.request.LoadRequest
import com.github.panpf.sketch.request.RequestInterceptor
import com.github.panpf.sketch.request.RequestInterceptor.Chain
import com.github.panpf.sketch.request.internal.MemoryCacheInterceptor
import com.github.panpf.sketch.request.internal.RequestContext
import com.github.panpf.sketch.request.internal.RequestInterceptorChain
import com.github.panpf.sketch.test.utils.TestAssets
import com.github.panpf.sketch.test.utils.getTestContextAndNewSketch
import com.github.panpf.sketch.util.asOrThrow
import com.github.panpf.tools4j.test.ktx.assertThrow
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MemoryCacheInterceptorTest {

    @Test
    fun testIntercept() {
        val (context, sketch) = getTestContextAndNewSketch()
        val memoryCache = sketch.memoryCache

        val requestInterceptorList = listOf(MemoryCacheInterceptor(), FakeRequestInterceptor())
        val executeRequest: (ImageRequest) -> ImageData = { request ->
            runBlocking {
                RequestInterceptorChain(
                    sketch = sketch,
                    initialRequest = request,
                    request = request,
                    requestContext = RequestContext(request),
                    interceptors = requestInterceptorList,
                    index = 0,
                ).proceed(request)
            }
        }

        memoryCache.clear()
        Assert.assertEquals(0, memoryCache.size)

        /* DownloadRequest */
        executeRequest(DownloadRequest(context, TestAssets.SAMPLE_JPEG_URI) {
            memoryCachePolicy(ENABLED)
        }).asOrThrow<DownloadData>()
        Assert.assertEquals(0, memoryCache.size)
        executeRequest(DownloadRequest(context, TestAssets.SAMPLE_JPEG_URI) {
            memoryCachePolicy(ENABLED)
        }).asOrThrow<DownloadData>()
        Assert.assertEquals(0, memoryCache.size)

        /* LoadRequest */
        executeRequest(LoadRequest(context, TestAssets.SAMPLE_JPEG_URI) {
            memoryCachePolicy(ENABLED)
        }).asOrThrow<LoadData>()
        Assert.assertEquals(0, memoryCache.size)
        executeRequest(LoadRequest(context, TestAssets.SAMPLE_JPEG_URI) {
            memoryCachePolicy(ENABLED)
        }).asOrThrow<LoadData>()
        Assert.assertEquals(0, memoryCache.size)

        /* DisplayRequest - ENABLED */
        val displayRequest = DisplayRequest(context, TestAssets.SAMPLE_JPEG_URI)
        val countBitmap: CountBitmap
        memoryCache.clear()
        Assert.assertEquals(0, memoryCache.size)
        executeRequest(displayRequest.newDisplayRequest {
            memoryCachePolicy(ENABLED)
        }).asOrThrow<DisplayData>().apply {
            Assert.assertEquals(DataFrom.LOCAL, dataFrom)
            countBitmap = drawable.asOrThrow<SketchCountBitmapDrawable>().countBitmap
        }
        Assert.assertEquals(40000, memoryCache.size)

        executeRequest(displayRequest.newDisplayRequest {
            memoryCachePolicy(ENABLED)
        }).asOrThrow<DisplayData>().apply {
            Assert.assertEquals(DataFrom.MEMORY_CACHE, dataFrom)
        }
        Assert.assertEquals(40000, memoryCache.size)

        /* DisplayRequest - DISABLED */
        memoryCache.clear()
        Assert.assertEquals(0, memoryCache.size)
        executeRequest(displayRequest.newDisplayRequest {
            memoryCachePolicy(DISABLED)
        }).asOrThrow<DisplayData>().apply {
            Assert.assertEquals(DataFrom.LOCAL, dataFrom)
        }
        Assert.assertEquals(0, memoryCache.size)

        memoryCache.put(displayRequest.cacheKey, countBitmap)
        Assert.assertEquals(40000, memoryCache.size)
        executeRequest(displayRequest.newDisplayRequest {
            memoryCachePolicy(DISABLED)
        }).asOrThrow<DisplayData>().apply {
            Assert.assertEquals(DataFrom.LOCAL, dataFrom)
        }
        Assert.assertEquals(40000, memoryCache.size)

        /* DisplayRequest - READ_ONLY */
        memoryCache.clear()
        Assert.assertEquals(0, memoryCache.size)
        executeRequest(displayRequest.newDisplayRequest {
            memoryCachePolicy(READ_ONLY)
        }).asOrThrow<DisplayData>().apply {
            Assert.assertEquals(DataFrom.LOCAL, dataFrom)
        }
        Assert.assertEquals(0, memoryCache.size)

        memoryCache.put(displayRequest.cacheKey, countBitmap)
        Assert.assertEquals(40000, memoryCache.size)
        executeRequest(displayRequest.newDisplayRequest {
            memoryCachePolicy(READ_ONLY)
        }).asOrThrow<DisplayData>().apply {
            Assert.assertEquals(DataFrom.MEMORY_CACHE, dataFrom)
        }
        Assert.assertEquals(40000, memoryCache.size)

        /* DisplayRequest - WRITE_ONLY */
        memoryCache.clear()
        Assert.assertEquals(0, memoryCache.size)
        executeRequest(displayRequest.newDisplayRequest {
            memoryCachePolicy(WRITE_ONLY)
        }).asOrThrow<DisplayData>().apply {
            Assert.assertEquals(DataFrom.LOCAL, dataFrom)
        }
        Assert.assertEquals(40000, memoryCache.size)

        executeRequest(displayRequest.newDisplayRequest {
            memoryCachePolicy(WRITE_ONLY)
        }).asOrThrow<DisplayData>().apply {
            Assert.assertEquals(DataFrom.LOCAL, dataFrom)
        }
        Assert.assertEquals(40000, memoryCache.size)

        /* Non SketchCountBitmapDrawable */
        val displayRequest1 = DisplayRequest(context, TestAssets.SAMPLE_PNG_URI)
        memoryCache.clear()
        Assert.assertEquals(0, memoryCache.size)
        executeRequest(displayRequest1.newDisplayRequest {
            memoryCachePolicy(ENABLED)
        }).asOrThrow<DisplayData>().apply {
            Assert.assertEquals(DataFrom.LOCAL, dataFrom)
        }
        Assert.assertEquals(0, memoryCache.size)

        executeRequest(displayRequest1.newDisplayRequest {
            memoryCachePolicy(ENABLED)
        }).asOrThrow<DisplayData>().apply {
            Assert.assertEquals(DataFrom.LOCAL, dataFrom)
        }
        Assert.assertEquals(0, memoryCache.size)

        /* Depth.MEMORY */
        memoryCache.clear()
        Assert.assertEquals(0, memoryCache.size)
        assertThrow(DepthException::class) {
            executeRequest(displayRequest.newDisplayRequest {
                memoryCachePolicy(ENABLED)
                depth(MEMORY)
            })
        }
    }

    @Test
    fun testEqualsAndHashCode() {
        val element1 = MemoryCacheInterceptor()
        val element11 = MemoryCacheInterceptor()
        val element2 = MemoryCacheInterceptor()

        Assert.assertNotSame(element1, element11)
        Assert.assertNotSame(element1, element2)
        Assert.assertNotSame(element2, element11)

        Assert.assertEquals(element1, element1)
        Assert.assertEquals(element1, element11)
        Assert.assertEquals(element1, element2)
        Assert.assertEquals(element2, element11)
        Assert.assertNotEquals(element1, null)
        Assert.assertNotEquals(element1, Any())

        Assert.assertEquals(element1.hashCode(), element1.hashCode())
        Assert.assertEquals(element1.hashCode(), element11.hashCode())
        Assert.assertEquals(element1.hashCode(), element2.hashCode())
        Assert.assertEquals(element2.hashCode(), element11.hashCode())
    }

    @Test
    fun testToString() {
        Assert.assertEquals("MemoryCacheInterceptor", MemoryCacheInterceptor().toString())
    }

    class FakeRequestInterceptor : RequestInterceptor {
        override suspend fun intercept(chain: Chain): ImageData {
            return when (chain.request) {
                is DisplayRequest -> {
                    val bitmap = Bitmap.createBitmap(100, 100, ARGB_8888)
                    val imageInfo: ImageInfo
                    val drawable = if (chain.request.uriString.contains(".jpeg")) {
                        imageInfo = ImageInfo(100, 100, "image/jpeg", 0)
                        val countBitmap = CountBitmap(
                            bitmap = bitmap,
                            sketch = chain.sketch,
                            imageUri = chain.request.uriString,
                            requestKey = chain.request.key,
                            requestCacheKey = chain.request.cacheKey,
                            imageInfo = imageInfo,
                            transformedList = null
                        )
                        SketchCountBitmapDrawable(
                            chain.sketch.context.resources,
                            countBitmap,
                            DataFrom.LOCAL
                        )
                    } else {
                        imageInfo = ImageInfo(100, 100, "image/png", 0)
                        BitmapDrawable(chain.sketch.context.resources, bitmap)
                    }
                    DisplayData(drawable, imageInfo, DataFrom.LOCAL, null)
                }
                is LoadRequest -> {
                    val bitmap = Bitmap.createBitmap(100, 100, ARGB_8888)
                    val imageInfo = ImageInfo(100, 100, "image/jpeg", 0)
                    LoadData(bitmap, imageInfo, DataFrom.LOCAL, null)
                }
                is DownloadRequest -> {
                    DownloadData.Bytes(byteArrayOf(), DataFrom.NETWORK)
                }
                else -> {
                    throw UnsupportedOperationException("Unsupported ImageRequest: ${chain.request::class.java}")
                }
            }
        }
    }
}