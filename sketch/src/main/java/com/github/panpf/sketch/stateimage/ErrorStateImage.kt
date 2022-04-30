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
package com.github.panpf.sketch.stateimage

import android.graphics.drawable.Drawable
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.internal.UriInvalidException
import com.github.panpf.sketch.stateimage.ErrorStateImage.Builder
import com.github.panpf.sketch.util.SketchException
import java.util.LinkedList

fun newErrorStateImage(
    defaultImage: StateImage,
    configBlock: (Builder.() -> Unit)? = null
): ErrorStateImage = Builder(defaultImage).apply {
    configBlock?.invoke(this)
}.build()

fun newErrorStateImageBuilder(
    defaultImage: StateImage,
    configBlock: (Builder.() -> Unit)? = null
): Builder = Builder(defaultImage).apply {
    configBlock?.invoke(this)
}

class ErrorStateImage private constructor(private val matcherList: List<Matcher>) : StateImage {

    override fun getDrawable(request: ImageRequest, exception: SketchException?): Drawable? =
        matcherList
            .find { it.match(request, exception) }
            ?.getDrawable(request, exception)

    class Builder(private val defaultImage: StateImage) {

        private val matcherList = LinkedList<Matcher>()

        fun addMatcher(matcher: Matcher): Builder = apply {
            matcherList.add(matcher)
        }

        fun uriEmptyError(emptyImage: StateImage): Builder = apply {
            addMatcher(UriEmptyMatcher(emptyImage))
        }

        fun uriEmptyError(emptyDrawable: Drawable): Builder = apply {
            addMatcher(UriEmptyMatcher(DrawableStateImage(emptyDrawable)))
        }

        fun uriEmptyError(emptyImageResId: Int): Builder = apply {
            addMatcher(UriEmptyMatcher(DrawableStateImage(emptyImageResId)))
        }

        fun build(): ErrorStateImage =
            ErrorStateImage(matcherList.plus(DefaultMatcher(defaultImage)))
    }

    interface Matcher {

        fun match(request: ImageRequest, exception: SketchException?): Boolean

        fun getDrawable(request: ImageRequest, throwable: SketchException?): Drawable?
    }

    private class DefaultMatcher(val stateImage: StateImage) : Matcher {

        override fun match(request: ImageRequest, exception: SketchException?): Boolean = true

        override fun getDrawable(request: ImageRequest, throwable: SketchException?): Drawable? =
            stateImage.getDrawable(request, throwable)
    }

    private class UriEmptyMatcher(val stateImage: StateImage) : Matcher {

        override fun match(request: ImageRequest, exception: SketchException?): Boolean =
            exception is UriInvalidException && (request.uriString.isEmpty() || request.uriString.isBlank())

        override fun getDrawable(request: ImageRequest, throwable: SketchException?): Drawable? =
            stateImage.getDrawable(request, throwable)
    }
}