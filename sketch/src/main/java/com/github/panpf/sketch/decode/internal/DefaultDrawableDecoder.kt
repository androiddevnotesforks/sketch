package com.github.panpf.sketch.decode.internal

import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.decode.DrawableDecodeResult
import com.github.panpf.sketch.decode.DrawableDecoder
import com.github.panpf.sketch.drawable.SketchBitmapDrawable
import com.github.panpf.sketch.fetch.FetchResult
import com.github.panpf.sketch.request.DisplayRequest
import kotlinx.coroutines.withContext

class DefaultDrawableDecoder(
    private val sketch: Sketch,
    private val request: DisplayRequest,
    private val fetchResult: FetchResult
) : DrawableDecoder {

    // todo support ImageDecoder gif wep animation heif animation
    override suspend fun decode(): DrawableDecodeResult =
        withContext(sketch.decodeTaskDispatcher) {
            val memoryCacheHelper = newBitmapMemoryCacheHelper(sketch, request)
            memoryCacheHelper?.lock?.lock()
            return@withContext try {
                memoryCacheHelper?.read()
                    ?: BitmapDecodeInterceptorChain(
                        initialRequest = request,
                        interceptors = sketch.bitmapDecodeInterceptors,
                        index = 0,
                        sketch = sketch,
                        request = request,
                        fetchResult = fetchResult
                    ).proceed(request).run {
                        val drawable = memoryCacheHelper?.write(this)
                            ?: SketchBitmapDrawable(
                                requestKey = request.key,
                                requestUri = request.uriString,
                                imageInfo = this.imageInfo,
                                exifOrientation = this.exifOrientation,
                                imageDataFrom = this.dataFrom,
                                transformedList = this.transformedList,
                                bitmap = this.bitmap
                            )
                        DrawableDecodeResult(
                            drawable = drawable,
                            imageInfo = this.imageInfo,
                            exifOrientation = this.exifOrientation,
                            dataFrom = this.dataFrom,
                            transformedList = this.transformedList
                        )
                    }
            } finally {
                memoryCacheHelper?.lock?.unlock()
            }
        }

    override fun close() {

    }

    class Factory : DrawableDecoder.Factory {
        override fun create(
            sketch: Sketch, request: DisplayRequest, fetchResult: FetchResult
        ): DrawableDecoder = DefaultDrawableDecoder(sketch, request, fetchResult)

        override fun toString(): String = "DefaultDrawableDecoder"
    }

}