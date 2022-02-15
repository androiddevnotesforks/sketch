/*
 * Copyright (C) 2019 panpf <panpfpanpf@outlook.com>
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
package com.github.panpf.sketch.util

import android.graphics.Bitmap

data class BitmapInfo constructor(
    val width: Int,
    val height: Int,
    val byteCount: Int,
    val config: Bitmap.Config?
) {
    override fun toString(): String =
        "BitmapInfo(width=$width, height=$height, byteCount=${byteCount.toLong().formatFileSize()}, config=$config)"

    fun toShortString(): String =
        "BitmapInfo(${width}x$height,${byteCount.toLong().formatFileSize()},$config)"
}

fun Bitmap.toBitmapInfo(): BitmapInfo = BitmapInfo(width, height, byteCountCompat, config)