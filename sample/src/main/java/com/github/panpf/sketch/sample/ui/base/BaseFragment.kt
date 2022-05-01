/*
 * Copyright (C) 2021 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.panpf.sketch.sample.ui.base

import androidx.fragment.app.Fragment
import com.github.panpf.sketch.sample.ui.common.ActionResult
import com.github.panpf.tools4a.toast.ktx.showLongToast

abstract class BaseFragment : Fragment() {

    fun handleActionResult(result: ActionResult): Boolean =
        when (result) {
            is ActionResult.Success -> {
                result.message?.let { showLongToast(it) }
                true
            }
            is ActionResult.Error -> {
                showLongToast(result.message)
                false
            }
        }
}