package com.github.panpf.sketch.request

import android.graphics.Bitmap
import com.github.panpf.sketch.datasource.DataFrom
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.sketch.util.SketchException

sealed interface LoadResult : ImageResult {

    data class Success constructor(
        override val request: LoadRequest,
        val bitmap: Bitmap,
        val imageInfo: ImageInfo,
        val dataFrom: DataFrom,
        val transformedList: List<String>?,
    ) : LoadResult, ImageResult.Success

    data class Error constructor(
        override val request: LoadRequest,
        override val exception: SketchException,
    ) : LoadResult, ImageResult.Error
}