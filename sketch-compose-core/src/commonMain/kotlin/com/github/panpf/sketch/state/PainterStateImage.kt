/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
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
package com.github.panpf.sketch.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.github.panpf.sketch.Image
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.asSketchImage
import com.github.panpf.sketch.painter.PainterEqualizer
import com.github.panpf.sketch.painter.rememberEqualityPainterResource
import com.github.panpf.sketch.request.ImageRequest
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi

@Composable
fun rememberPainterStateImage(painter: PainterEqualizer): PainterStateImage =
    remember(painter) { PainterStateImage(painter) }

@OptIn(ExperimentalResourceApi::class)
@Composable
fun rememberPainterStateImage(resource: DrawableResource): PainterStateImage {
    val painter = rememberEqualityPainterResource(resource)
    return remember(resource) { PainterStateImage(painter) }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun PainterStateImage(resource: DrawableResource): PainterStateImage {
    val painter = rememberEqualityPainterResource(resource)
    return PainterStateImage(painter)
}

@Stable
class PainterStateImage(val painter: PainterEqualizer) : StateImage {

    override val key: String = "PainterStateImage(${painter.key})"

    override fun getImage(sketch: Sketch, request: ImageRequest, throwable: Throwable?): Image {
        return painter.wrapped.asSketchImage()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PainterStateImage) return false
        return painter == other.painter
    }

    override fun hashCode(): Int {
        return painter.hashCode()
    }

    override fun toString(): String {
        return "PainterStateImage(painter=$painter)"
    }
}