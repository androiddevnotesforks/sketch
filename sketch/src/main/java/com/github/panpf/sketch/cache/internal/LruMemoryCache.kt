/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.panpf.sketch.cache.internal

import android.content.ComponentCallbacks2
import androidx.collection.LruCache
import com.github.panpf.sketch.cache.CountBitmap
import com.github.panpf.sketch.cache.MemoryCache
import com.github.panpf.sketch.util.Logger
import com.github.panpf.sketch.util.allocationByteCountCompat
import com.github.panpf.sketch.util.format
import com.github.panpf.sketch.util.formatFileSize
import com.github.panpf.sketch.util.getTrimLevelName
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.roundToInt

/**
 * A bitmap memory cache that manages the cache according to a least-used rule
 */
class LruMemoryCache constructor(override val maxSize: Long) : MemoryCache {

    companion object {
        private const val MODULE = "LruMemoryCache"
    }

    private val cache: LruCache<String, CountBitmap> =
        object : LruCache<String, CountBitmap>(maxSize.toInt()) {
            override fun sizeOf(key: String, countBitmap: CountBitmap): Int {
                val bitmapSize = countBitmap.byteCount
                return if (bitmapSize == 0) 1 else bitmapSize
            }

            override fun entryRemoved(
                evicted: Boolean, key: String, old: CountBitmap, new: CountBitmap?
            ) {
                logger?.w(MODULE) {
                    "removed. ${old.info}. ${size.formatFileSize()}. $key"
                }
                old.setIsCached(false, MODULE)
            }
        }
    private val getCount = AtomicInteger()
    private val hitCount = AtomicInteger()

    override var logger: Logger? = null
    override val size: Long
        get() = cache.size().toLong()

    override fun put(key: String, countBitmap: CountBitmap): Boolean {
        val bitmap = countBitmap.bitmap ?: return false
        if (bitmap.allocationByteCountCompat >= maxSize * 0.7f) {
            logger?.w(MODULE) {
                val bitmapSize = bitmap.allocationByteCountCompat.formatFileSize()
                "put. reject. Bitmap too big ${bitmapSize}, maxSize is ${maxSize.formatFileSize()}, ${countBitmap.info}"
            }
            return false
        }
        return if (cache[key] == null) {
            countBitmap.setIsCached(true, MODULE)
            cache.put(key, countBitmap)
            logger?.d(MODULE) {
                "put. successful. ${countBitmap.info}. ${size.formatFileSize()}. $key"
            }
            true
        } else {
            logger?.w(MODULE, "put. exist. ${countBitmap.info}. key=$key")
            false
        }
    }

    override fun remove(key: String): CountBitmap? = cache.remove(key)

    override fun get(key: String): CountBitmap? =
        cache[key]?.takeIf {
            (!it.isRecycled).apply {
                if (!this) {
                    cache.remove(key)
                }
            }
        }.apply {
            val getCount1 = getCount.addAndGet(1)
            val hitCount1 = if (this != null) {
                hitCount.addAndGet(1)
            } else {
                hitCount.get()
            }
            if (getCount1 == Int.MAX_VALUE || hitCount1 == Int.MAX_VALUE) {
                getCount.set(0)
                hitCount.set(0)
            }
            logger?.d(MODULE) {
                val hitRatio = ((hitCount1.toFloat() / getCount1).format(2) * 100).roundToInt()
                if (this != null) {
                    "get. hit($hitRatio%). ${this.info}}. $key"
                } else {
                    "get. miss($hitRatio%). $key"
                }
            }
        }

    override fun exist(key: String): Boolean =
        cache[key]?.takeIf {
            (!it.isRecycled).apply {
                if (!this) {
                    cache.remove(key)
                }
            }
        } != null

    override fun trim(level: Int) {
        val oldSize = size
        if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
            cache.evictAll()
        } else if (level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            cache.trimToSize(cache.maxSize() / 2)
        }
        val releasedSize = oldSize - size
        logger?.w(
            MODULE,
            "trim. level '${getTrimLevelName(level)}', released ${releasedSize.formatFileSize()}, size ${size.formatFileSize()}"
        )
    }

    override fun clear() {
        val oldSize = size
        cache.evictAll()
        logger?.w(MODULE, "clear. cleared ${oldSize.formatFileSize()}")
    }

    override fun toString(): String = "$MODULE(maxSize=${maxSize.formatFileSize()})"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LruMemoryCache) return false
        if (maxSize != other.maxSize) return false
        return true
    }

    override fun hashCode(): Int {
        return maxSize.hashCode()
    }
}