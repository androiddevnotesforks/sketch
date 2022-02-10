package com.github.panpf.sketch.request.internal

import androidx.annotation.MainThread
import com.github.panpf.sketch.request.DisplayData
import com.github.panpf.sketch.request.DisplayOptions
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.RequestInterceptor
import com.github.panpf.sketch.request.RequestInterceptor.Chain

class DefaultDisplayOptionsInterceptor(
    private val defaultDisplayOptions: DisplayOptions?
) : RequestInterceptor<DisplayRequest, DisplayData> {

    @MainThread
    override suspend fun intercept(chain: Chain<DisplayRequest, DisplayData>): DisplayData {
        val request = if (defaultDisplayOptions?.isEmpty() == true) {
            chain.request.newDisplayRequest {
                options(defaultDisplayOptions, requestFirst = true)
            }
        } else {
            chain.request
        }
        return chain.proceed(request)
    }

    override fun toString(): String = "DefaultDisplayOptionsInterceptor"
}