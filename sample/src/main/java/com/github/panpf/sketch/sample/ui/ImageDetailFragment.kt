package com.github.panpf.sketch.sample.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.github.panpf.sketch.displayImage
import com.github.panpf.sketch.sample.appSettingsService
import com.github.panpf.sketch.sample.base.BindingFragment
import com.github.panpf.sketch.sample.bean.ImageDetail
import com.github.panpf.sketch.sample.databinding.FragmentImageDetailBinding
import com.github.panpf.sketch.stateimage.StateImage
import com.github.panpf.sketch.viewability.showRingProgressIndicator
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ImageDetailFragment : BindingFragment<FragmentImageDetailBinding>() {

    private val args by navArgs<ImageDetailFragmentArgs>()

    override fun createViewBinding(inflater: LayoutInflater, parent: ViewGroup?) =
        FragmentImageDetailBinding.inflate(inflater, parent, false)

    override fun onInitViews(binding: FragmentImageDetailBinding, savedInstanceState: Bundle?) {
        super.onInitViews(binding, savedInstanceState)
        binding.imageFragmentZoomImageView.apply {
            showRingProgressIndicator()
            zoomAbility.readModeEnabled = true
            zoomAbility.showTileBounds = args.showTileMap && appSettingsService.showTileBoundsInHugeImagePage.value
        }
    }

    override fun onInitData(binding: FragmentImageDetailBinding, savedInstanceState: Bundle?) {
        val imageDetail = Json.decodeFromString<ImageDetail>(args.imageDetailJson)

        binding.imageFragmentZoomImageView.apply {
            displayImage(imageDetail.firstMiddenUrl) {
                placeholderImage(
                    StateImage.memoryCache(
                        imageDetail.placeholderImageMemoryKey,
                        null
                    )
                )
            }
        }

        binding.imageFragmentTileMapImageView.apply {
            isVisible = args.showTileMap
            if (args.showTileMap) {
                setZoomImageView(binding.imageFragmentZoomImageView)
                displayImage(imageDetail.firstMiddenUrl)
            }
        }
    }
}