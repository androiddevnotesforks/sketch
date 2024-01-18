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
package com.github.panpf.sketch.sample.ui.gallery

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.github.panpf.assemblyadapter.recycler.ItemSpan
import com.github.panpf.assemblyadapter.recycler.divider.Divider
import com.github.panpf.assemblyadapter.recycler.divider.newAssemblyGridDividerItemDecoration
import com.github.panpf.assemblyadapter.recycler.divider.newAssemblyStaggeredGridDividerItemDecoration
import com.github.panpf.assemblyadapter.recycler.newAssemblyGridLayoutManager
import com.github.panpf.assemblyadapter.recycler.newAssemblyStaggeredGridLayoutManager
import com.github.panpf.assemblyadapter.recycler.paging.AssemblyPagingDataAdapter
import com.github.panpf.sketch.sample.NavMainDirections
import com.github.panpf.sketch.sample.R
import com.github.panpf.sketch.sample.appSettingsService
import com.github.panpf.sketch.sample.databinding.FragmentRecyclerRefreshBinding
import com.github.panpf.sketch.sample.model.ImageDetail
import com.github.panpf.sketch.sample.model.LayoutMode
import com.github.panpf.sketch.sample.model.LayoutMode.GRID
import com.github.panpf.sketch.sample.model.LayoutMode.STAGGERED_GRID
import com.github.panpf.sketch.sample.model.Photo
import com.github.panpf.sketch.sample.ui.base.BaseBindingFragment
import com.github.panpf.sketch.sample.ui.common.list.LoadStateItemFactory
import com.github.panpf.sketch.sample.ui.common.list.MyLoadStateAdapter
import com.github.panpf.sketch.sample.ui.common.list.findPagingAdapter
import com.github.panpf.sketch.sample.util.ignoreFirst
import com.github.panpf.sketch.sample.util.repeatCollectWithLifecycle
import com.github.panpf.tools4k.lang.asOrThrow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

abstract class BasePhotoListViewFragment :
    BaseBindingFragment<FragmentRecyclerRefreshBinding>() {

    abstract val animatedPlaceholder: Boolean
    abstract val photoPagingFlow: Flow<PagingData<Photo>>

    private var pagingFlowCollectJob: Job? = null
    private var loadStateFlowCollectJob: Job? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(
        binding: FragmentRecyclerRefreshBinding,
        savedInstanceState: Bundle?
    ) {
        binding.myRecycler.apply {
            appSettingsService.photoListLayoutMode
                .repeatCollectWithLifecycle(viewLifecycleOwner, State.STARTED) {
                    val (layoutManager1, itemDecoration) =
                        newLayoutManagerAndItemDecoration(LayoutMode.valueOf(it))
                    layoutManager = layoutManager1
                    (0 until itemDecorationCount).forEach { index ->
                        removeItemDecorationAt(index)
                    }
                    addItemDecoration(itemDecoration)

                    val pagingAdapter = newPagingAdapter(binding)
                    val loadStateAdapter = MyLoadStateAdapter().apply {
                        noDisplayLoadStateWhenPagingEmpty(pagingAdapter)
                    }
                    adapter = pagingAdapter.withLoadStateFooter(loadStateAdapter)

                    bindRefreshAndAdapter(binding, pagingAdapter)
                }

            appSettingsService.listsCombinedFlow.ignoreFirst()
                .repeatCollectWithLifecycle(viewLifecycleOwner, State.STARTED) {
                    adapter?.notifyDataSetChanged()
                }
            appSettingsService.ignoreExifOrientation.ignoreFirst()
                .repeatCollectWithLifecycle(viewLifecycleOwner, State.STARTED) {
                    adapter?.findPagingAdapter()?.refresh()
                }
        }
    }

    private fun newLayoutManagerAndItemDecoration(layoutMode: LayoutMode): Pair<LayoutManager, ItemDecoration> {
        val layoutManager: LayoutManager
        val itemDecoration: ItemDecoration
        when (layoutMode) {
            GRID -> {
                layoutManager =
                    requireContext().newAssemblyGridLayoutManager(3, GridLayoutManager.VERTICAL) {
                        itemSpanByItemFactory(LoadStateItemFactory::class, ItemSpan.fullSpan())
                    }
                itemDecoration = requireContext().newAssemblyGridDividerItemDecoration {
                    val gridDivider =
                        requireContext().resources.getDimensionPixelSize(R.dimen.grid_divider)
                    divider(Divider.space(gridDivider))
                    sideDivider(Divider.space(gridDivider))
                    useDividerAsHeaderAndFooterDivider()
                    useSideDividerAsSideHeaderAndFooterDivider()
                }
            }

            STAGGERED_GRID -> {
                layoutManager = newAssemblyStaggeredGridLayoutManager(
                    3,
                    StaggeredGridLayoutManager.VERTICAL
                ) {
                    fullSpanByItemFactory(LoadStateItemFactory::class)
                }
                itemDecoration = requireContext().newAssemblyStaggeredGridDividerItemDecoration {
                    val gridDivider =
                        requireContext().resources.getDimensionPixelSize(R.dimen.grid_divider)
                    divider(Divider.space(gridDivider))
                    sideDivider(Divider.space(gridDivider))
                    useDividerAsHeaderAndFooterDivider()
                    useSideDividerAsSideHeaderAndFooterDivider()
                }
            }
        }
        return layoutManager to itemDecoration
    }

    private fun newPagingAdapter(binding: FragmentRecyclerRefreshBinding): PagingDataAdapter<*, *> {
        return AssemblyPagingDataAdapter<Photo>(listOf(
            PhotoGridItemFactory(animatedPlaceholder = animatedPlaceholder)
                .setOnViewClickListener(R.id.myListImage) { _, _, _, absoluteAdapterPosition, _ ->
                    startImageDetail(binding, absoluteAdapterPosition)
                }
        )).apply {
            pagingFlowCollectJob?.cancel()
            pagingFlowCollectJob = viewLifecycleOwner.lifecycleScope.launch {
                photoPagingFlow.collect {
                    submitData(it)
                }
            }
        }
    }

    private fun bindRefreshAndAdapter(
        binding: FragmentRecyclerRefreshBinding,
        pagingAdapter: PagingDataAdapter<*, *>
    ) {
        binding.swipeRefresh.setOnRefreshListener {
            pagingAdapter.refresh()
        }
        loadStateFlowCollectJob?.cancel()
        loadStateFlowCollectJob =
            viewLifecycleOwner.lifecycleScope.launch {
                pagingAdapter.loadStateFlow.collect { loadStates ->
                    when (val refreshState = loadStates.refresh) {
                        is LoadState.Loading -> {
                            binding.state.gone()
                            binding.swipeRefresh.isRefreshing = true
                        }

                        is LoadState.Error -> {
                            binding.swipeRefresh.isRefreshing = false
                            binding.state.error {
                                message(refreshState.error)
                                retryAction {
                                    pagingAdapter.refresh()
                                }
                            }
                        }

                        is LoadState.NotLoading -> {
                            binding.swipeRefresh.isRefreshing = false
                            if (pagingAdapter.itemCount <= 0) {
                                binding.state.empty {
                                    message("No Photos")
                                }
                            } else {
                                binding.state.gone()
                            }
                        }
                    }
                }
            }
    }

    private fun startImageDetail(binding: FragmentRecyclerRefreshBinding, position: Int) {
        val items = binding.myRecycler
            .adapter!!.asOrThrow<ConcatAdapter>()
            .adapters.first().asOrThrow<AssemblyPagingDataAdapter<Photo>>()
            .currentList
        val totalCount = items.size
        val startPosition = (position - 50).coerceAtLeast(0)
        val endPosition = (position + 50).coerceAtMost(totalCount - 1)
        val imageList = items.asSequence()
            .filterNotNull()
            .filterIndexed { index, _ -> index in startPosition..endPosition }
            .map {
                ImageDetail(
                    originUrl = it.originalUrl,
                    mediumUrl = it.detailPreviewUrl,
                    thumbnailUrl = it.listThumbnailUrl,
                )
            }.toList()
        findNavController().navigate(
            NavMainDirections.actionPhotoPagerViewFragment(
                imageDetailJsonArray = Json.encodeToString(imageList),
                totalCount = totalCount,
                startPosition = startPosition,
                initialPosition = position
            ),
        )
    }
}