package com.github.panpf.sketch.compose.sample.util

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.github.panpf.sketch.util.isAttachedToWindowCompat

fun <T> safeRun(block: () -> T): T? {
    return try {
        block()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun <T> LiveData<T>.observeFromView(view: View, observer: Observer<T>) {
    if (view.isAttachedToWindowCompat) {
        observeForever(observer)
    }
    view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View?) {
            try {
                observeForever(observer)
            } catch (e: IllegalArgumentException) {
            }
        }

        override fun onViewDetachedFromWindow(v: View?) {
            removeObserver(observer)
        }
    })
}

fun <T> LiveData<T>.observeFromViewAndInit(view: View, observer: Observer<T>) {
    if (view.isAttachedToWindowCompat) {
        observeForever(observer)
    } else {
        observer.onChanged(value)
    }
    view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View?) {
            try {
                observeForever(observer)
            } catch (e: IllegalArgumentException) {
            }
        }

        override fun onViewDetachedFromWindow(v: View?) {
            removeObserver(observer)
        }
    })
}