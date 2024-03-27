package com.github.panpf.sketch.fetch

import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.annotation.WorkerThread
import com.github.panpf.sketch.datasource.ResourceDataSource
import com.github.panpf.sketch.fetch.ResourceUriFetcher.Companion.SCHEME
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.util.MimeTypeMap
import com.github.panpf.sketch.util.ifOrNull
import com.github.panpf.sketch.util.toUri

/**
 * Sample: 'ios.resource://test.png'
 */
fun newResourceUri(resourceName: String): String = "$SCHEME://$resourceName"

class ResourceUriFetcher(
    val sketch: Sketch,
    val request: ImageRequest,
    val resourceName: String,
) : Fetcher {

    companion object {
        const val SCHEME = "ios.resource"
    }

    @WorkerThread
    override suspend fun fetch(): Result<FetchResult> = kotlin.runCatching {
        val mimeType = MimeTypeMap.getMimeTypeFromUrl(resourceName)
        return Result.success(
            FetchResult(
                ResourceDataSource(sketch, request, resourceName),
                mimeType
            )
        )
    }

    class Factory : Fetcher.Factory {

        override fun create(sketch: Sketch, request: ImageRequest): ResourceUriFetcher? {
            val uri = request.uriString.toUri()
            return ifOrNull(SCHEME.equals(uri.scheme, ignoreCase = true)) {
                ResourceUriFetcher(sketch, request, uri.authority.orEmpty())
            }
        }

        override fun toString(): String = "ResourceUriFetcher"

        @Suppress("RedundantOverride")
        override fun equals(other: Any?): Boolean {
            // If you add construction parameters to this class, you need to change it here
            return super.equals(other)
        }

        @Suppress("RedundantOverride")
        override fun hashCode(): Int {
            // If you add construction parameters to this class, you need to change it here
            return super.hashCode()
        }
    }
}