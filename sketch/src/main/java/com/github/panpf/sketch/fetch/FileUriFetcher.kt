package com.github.panpf.sketch.fetch

import android.webkit.MimeTypeMap
import com.github.panpf.sketch.datasource.FileDataSource
import com.github.panpf.sketch.fetch.FileUriFetcher.Companion.SCHEME
import com.github.panpf.sketch.request.ImageRequest
import java.io.File

/**
 * Sample: 'file:///sdcard/sample.jpg'
 */
fun newFileUri(filePath: String): String = "$SCHEME://$filePath"

/**
 * Support 'file:///sdcard/sample.jpg', '/sdcard/sample.jpg' uri
 */
class FileUriFetcher(
    val request: ImageRequest,
    val file: File,
) : Fetcher {

    companion object {
        const val SCHEME = "file"
    }

    override suspend fun fetch(): FetchResult {
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
        return FetchResult(FileDataSource(request, file), mimeType)
    }

    class Factory : Fetcher.Factory {
        override fun create(request: ImageRequest): FileUriFetcher? {
            val uriString = request.uriString
            val subStartIndex = when {
                SCHEME.equals(request.uri.scheme, ignoreCase = true) -> SCHEME.length + 3
                uriString.startsWith("/") -> 0
                else -> -1
            }
            if (subStartIndex != -1) {
                val subEndIndex = uriString.indexOf("?").takeIf { it != -1 }
                    ?: uriString.indexOf("#").takeIf { it != -1 }
                    ?: uriString.length
                val file = File(uriString.substring(subStartIndex, subEndIndex))
                return FileUriFetcher(request, file)
            }
            return null
        }

        override fun toString(): String = "FileUriFetcher"
    }
}