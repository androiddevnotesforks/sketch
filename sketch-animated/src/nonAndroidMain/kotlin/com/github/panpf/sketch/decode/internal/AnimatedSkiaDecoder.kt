package com.github.panpf.sketch.decode.internal

import com.github.panpf.sketch.SkiaAnimatedImage
import com.github.panpf.sketch.datasource.DataSource
import com.github.panpf.sketch.decode.DecodeResult
import com.github.panpf.sketch.decode.Decoder
import com.github.panpf.sketch.decode.ExifOrientation
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.sketch.request.animationEndCallback
import com.github.panpf.sketch.request.animationStartCallback
import com.github.panpf.sketch.request.internal.RequestContext
import com.github.panpf.sketch.request.repeatCount
import okio.buffer
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data

open class AnimatedSkiaDecoder(
    private val requestContext: RequestContext,
    private val dataSource: DataSource,
) : Decoder {

    override suspend fun decode(): Result<DecodeResult> = runCatching {
        val bytes = dataSource.openSource().buffer().readByteArray()
        val data = Data.makeFromBytes(bytes)
        val codec = Codec.makeFromData(data)
        val mimeType = "image/${codec.encodedImageFormat.name.lowercase()}"
        val imageInfo = ImageInfo(
            width = codec.width,
            height = codec.height,
            mimeType = mimeType,
            exifOrientation = ExifOrientation.UNDEFINED
        )
        // TODO not support resize
        val request = requestContext.request
        val repeatCount = request.repeatCount
        DecodeResult(
            image = SkiaAnimatedImage(
                codec = codec,
                repeatCount = repeatCount,
                animationStartCallback = request.animationStartCallback,
                animationEndCallback = request.animationEndCallback
            ),
            imageInfo = imageInfo,
            dataFrom = dataSource.dataFrom,
            transformedList = null,
            extras = null,
        )
    }
}