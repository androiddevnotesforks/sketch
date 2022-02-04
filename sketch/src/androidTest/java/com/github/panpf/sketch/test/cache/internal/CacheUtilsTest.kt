package com.github.panpf.sketch.test.cache.internal

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.github.panpf.sketch.cache.internal.defaultMemoryCacheBytes
import com.github.panpf.sketch.cache.internal.getAppMemoryClassBytes
import com.github.panpf.sketch.cache.internal.isLowRamDevice
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.roundToInt

@RunWith(AndroidJUnit4::class)
class CacheUtilsTest {

    @Test
    fun testGetAppMemoryClassBytes() {
        val context = InstrumentationRegistry.getContext()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        val isLargeHeap =
            (context.applicationInfo.flags and ApplicationInfo.FLAG_LARGE_HEAP) != 0
        val appMemoryClassBytes = when {
            activityManager != null && isLargeHeap -> activityManager.largeMemoryClass * 1024 * 1024
            activityManager != null && !isLargeHeap -> activityManager.memoryClass * 1024 * 1024
            else -> 16 * 1024 * 1024
        }
        Assert.assertEquals(appMemoryClassBytes, context.getAppMemoryClassBytes())
    }

    @Test
    fun testIsLowMemoryDevice() {
        val context = InstrumentationRegistry.getContext()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        val isLowRamDevice =
            VERSION.SDK_INT < VERSION_CODES.KITKAT || activityManager?.isLowRamDevice == true
        Assert.assertEquals(isLowRamDevice, context.isLowRamDevice())
    }

    @Test
    fun testDefaultMemoryCacheBytes() {
        val context = InstrumentationRegistry.getContext()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        val isLowRamDevice =
            VERSION.SDK_INT < VERSION_CODES.KITKAT || activityManager?.isLowRamDevice == true
        val isLargeHeap =
            (context.applicationInfo.flags and ApplicationInfo.FLAG_LARGE_HEAP) != 0
        val appMemoryClassBytes = when {
            activityManager != null && isLargeHeap -> activityManager.largeMemoryClass * 1024 * 1024
            activityManager != null && !isLargeHeap -> activityManager.memoryClass * 1024 * 1024
            else -> 16 * 1024 * 1024
        }
        val maxCacheBytes =
            ((if (isLowRamDevice) 0.33f else 0.4f) * appMemoryClassBytes).roundToInt()
        val displayMetrics = context.resources.displayMetrics
        val screenBytes = displayMetrics.widthPixels * displayMetrics.heightPixels * 4
        // Memory is expected to cache images for up to six screens
        val expectCacheBytes = screenBytes * 6
        val defaultMemoryCacheBytes = expectCacheBytes.coerceAtMost(maxCacheBytes)
        Assert.assertEquals(defaultMemoryCacheBytes, context.defaultMemoryCacheBytes())
    }
}