package com.github.panpf.sketch.sample.util

import android.graphics.ColorSpace
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.annotation.MainThread
import com.github.panpf.sketch.cache.CachePolicy.DISABLED
import com.github.panpf.sketch.decode.BitmapConfig
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.ImageData
import com.github.panpf.sketch.request.RequestInterceptor
import com.github.panpf.sketch.request.RequestInterceptor.Chain
import com.github.panpf.sketch.request.pauseLoadWhenScrolling
import com.github.panpf.sketch.request.saveCellularTraffic
import com.github.panpf.sketch.sample.prefsService
import com.github.panpf.sketch.sample.widget.MyListImageView
import com.github.panpf.sketch.target.ViewTarget

class SettingsDisplayRequestInterceptor : RequestInterceptor {

    @MainThread
    override suspend fun intercept(chain: Chain): ImageData {
        val request = chain.request
        if (request !is DisplayRequest) {
            return chain.proceed(request)
        }

        val newRequest = request.newDisplayRequest {
            val prefsService = request.context.prefsService
            if (prefsService.disabledBitmapMemoryCache.value) {
                bitmapMemoryCachePolicy(DISABLED)
            }
            if (prefsService.disabledDownloadDiskCache.value) {
                downloadDiskCachePolicy(DISABLED)
            }
            if (prefsService.disabledBitmapResultDiskCache.value) {
                bitmapResultDiskCachePolicy(DISABLED)
            }
            if (prefsService.disabledReuseBitmap.value) {
                disabledReuseBitmap(true)
            }
            if (prefsService.ignoreExifOrientation.value) {
                ignoreExifOrientation(true)
            }
            if (VERSION.SDK_INT < VERSION_CODES.N && prefsService.inPreferQualityOverSpeed.value) {
                @Suppress("DEPRECATION")
                preferQualityOverSpeed(true)
            }
            when (prefsService.bitmapQuality.value) {
                "LOW" -> bitmapConfig(BitmapConfig.LOW_QUALITY)
                "MIDDEN" -> bitmapConfig(BitmapConfig.MIDDEN_QUALITY)
                "HIGH" -> bitmapConfig(BitmapConfig.HIGH_QUALITY)
            }
            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                when (val value = prefsService.colorSpace.value) {
                    "Default" -> {

                    }
                    else -> {
                        colorSpace(ColorSpace.get(ColorSpace.Named.valueOf(value)))
                    }
                }
            }
            val target = chain.request.target
            if (target is ViewTarget<*>) {
                val view = target.view
                if (view is MyListImageView) {
                    if (prefsService.disabledAnimatedImageInList.value) {
                        disabledAnimatedImage(true)
                    }
                    if (prefsService.pauseLoadWhenScrollInList.value) {
                        pauseLoadWhenScrolling(true)
                    }
                    if (prefsService.saveCellularTrafficInList.value) {
                        saveCellularTraffic(true)
                    }
                }
            }
        }
        return chain.proceed(newRequest)
    }

    override fun toString(): String = "SettingsDisplayRequestInterceptor"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}