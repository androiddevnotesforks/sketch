@file:Suppress("DEPRECATION")

package com.github.panpf.sketch.decode

import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Bitmap.Config.RGB_565
import android.graphics.Movie
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.exifinterface.media.ExifInterface
import com.github.panpf.sketch.ImageFormat
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.datasource.DataSource
import com.github.panpf.sketch.drawable.MovieDrawable
import com.github.panpf.sketch.drawable.SketchAnimatableDrawable
import com.github.panpf.sketch.fetch.FetchResult
import com.github.panpf.sketch.fetch.internal.isGif
import com.github.panpf.sketch.request.ANIMATION_REPEAT_INFINITE
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.animatable2CompatCallbackOf
import com.github.panpf.sketch.request.animatedTransformation
import com.github.panpf.sketch.request.animationEndCallback
import com.github.panpf.sketch.request.animationStartCallback
import com.github.panpf.sketch.request.internal.RequestExtras
import com.github.panpf.sketch.request.repeatCount
import java.util.Base64.Decoder

/**
 * A [Decoder] that uses [Movie] to decode GIFs.
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
class GifMovieDrawableDecoder constructor(
    private val sketch: Sketch,
    private val request: DisplayRequest,
    private val dataSource: DataSource,
) : DrawableDecoder {

    @WorkerThread
    override suspend fun decode(): DrawableDecodeResult {
        val movie: Movie? = dataSource.newInputStream().use { Movie.decodeStream(it) }

        val width = movie?.width() ?: 0
        val height = movie?.height() ?: 0
        check(movie != null && width > 0 && height > 0) { "Failed to decode GIF." }

        val movieDrawable = MovieDrawable(
            movie = movie,
            config = when {
                movie.isOpaque && request.bitmapConfig?.isLowQuality == true -> RGB_565
                else -> ARGB_8888
            },
            sketch.bitmapPool,
        )

        movieDrawable.setRepeatCount(request.repeatCount() ?: ANIMATION_REPEAT_INFINITE)

        // Set the start and end animation callbacks if any one is supplied through the request.
        val onStart = request.animationStartCallback()
        val onEnd = request.animationEndCallback()
        if (onStart != null || onEnd != null) {
            movieDrawable.registerAnimationCallback(animatable2CompatCallbackOf(onStart, onEnd))
        }

        // Set the animated transformation to be applied on each frame.
        movieDrawable.setAnimatedTransformation(request.animatedTransformation())

        val imageInfo = ImageInfo(width, height, ImageFormat.GIF.mimeType)

        return DrawableDecodeResult(
            drawable = SketchAnimatableDrawable(
                requestKey = request.key,
                requestUri = request.uriString,
                imageInfo = imageInfo,
                imageExifOrientation = ExifInterface.ORIENTATION_UNDEFINED,
                dataFrom = dataSource.dataFrom,
                transformedList = null,
                animatableDrawable = movieDrawable,
                "MovieDrawable"
            ),
            imageInfo = imageInfo,
            exifOrientation = ExifInterface.ORIENTATION_UNDEFINED,
            dataFrom = dataSource.dataFrom
        )
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    class Factory : DrawableDecoder.Factory {

        override fun create(
            sketch: Sketch,
            request: DisplayRequest,
            requestExtras: RequestExtras,
            fetchResult: FetchResult
        ): GifMovieDrawableDecoder? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && request.disabledAnimationDrawable != true
            ) {
                val imageFormat = ImageFormat.valueOfMimeType(fetchResult.mimeType)
                // Some sites disguise the suffix of a GIF file as a JPEG, which must be identified by the file header
                if (imageFormat == ImageFormat.GIF || fetchResult.headerBytes.isGif()) {
                    return GifMovieDrawableDecoder(sketch, request, fetchResult.dataSource)
                }
            }
            return null
        }

        override fun toString(): String = "GifDrawableDecoder"
    }
}