package com.github.panpf.sketch.decode

import androidx.annotation.WorkerThread
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.fetch.FetchResult
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.internal.RequestContext

interface DrawableDecoder {

    @WorkerThread
    suspend fun decode(): DrawableDecodeResult

    fun interface Factory {

        fun create(
            sketch: Sketch,
            request: ImageRequest,
            requestContext: RequestContext,
            fetchResult: FetchResult,
        ): DrawableDecoder?
    }
}