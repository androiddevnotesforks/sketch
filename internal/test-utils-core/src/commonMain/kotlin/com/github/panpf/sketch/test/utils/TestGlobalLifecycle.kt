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
package com.github.panpf.sketch.test.utils

import com.github.panpf.sketch.target.TargetLifecycle

/**
 * A [TargetLifecycle] implementation that is always resumed and never destroyed.
 *
 * This is used as a fallback if [findTargetLifecycle] cannot find a more tightly scoped [TargetLifecycle].
 */
object TestGlobalTargetLifecycle : TargetLifecycle() {

    override val currentState: State
        get() = State.RESUMED

    override fun addObserver(observer: EventObserver) {
        // Call the TargetLifecycle methods in order and do not hold a reference to the observer.
        observer.onStateChanged(this@TestGlobalTargetLifecycle, Event.ON_CREATE)
        observer.onStateChanged(this@TestGlobalTargetLifecycle, Event.ON_START)
        observer.onStateChanged(this@TestGlobalTargetLifecycle, Event.ON_RESUME)
    }

    override fun removeObserver(observer: EventObserver) {}

    override fun toString() = "GlobalTargetLifecycle"
}

fun TargetLifecycle.isTestGlobalTargetLifecycle() = this is TestGlobalTargetLifecycle