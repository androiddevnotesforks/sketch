package com.github.panpf.sketch.sample.ui.photo.pexels

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.compose.AsyncImageState
import com.github.panpf.sketch.request.DisplayResult

fun Modifier.mimeTypeLogo(
    state: AsyncImageState,
    painterMap: Map<String, Painter>,
    margin: Dp = 0.dp
): Modifier {
    return this.then(MimeTypeLogoElement(state, painterMap, margin))
}

internal data class MimeTypeLogoElement(
    val state: AsyncImageState,
    val painterMap: Map<String, Painter>,
    val margin: Dp
) : ModifierNodeElement<MimeTypeLogoNode>() {

    override fun create(): MimeTypeLogoNode {
        painterMap.entries.forEach {
            require(it.value.intrinsicSize.isSpecified) { "Painter.intrinsicSize must be specified: ${it.key}" }
        }
        return MimeTypeLogoNode(state, painterMap, margin)
    }

    override fun update(node: MimeTypeLogoNode) {
        painterMap.entries.forEach {
            require(it.value.intrinsicSize.isSpecified) { "Painter.intrinsicSize must be specified: ${it.key}" }
        }
        node.update(state, painterMap, margin)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "MimeTypeLogo"
        properties["mimeType"] = state.result
            ?.let { (it as? DisplayResult.Success) }
            ?.imageInfo?.mimeType
            ?: "null"
        properties["loadState"] = state.loadState?.name ?: "null"
    }
}

internal class MimeTypeLogoNode(
    private var state: AsyncImageState,
    private var painterMap: Map<String, Painter>,
    private var margin: Dp
) : Modifier.Node(), DrawModifierNode, CompositionLocalConsumerModifierNode {

    override fun ContentDrawScope.draw() {
        drawContent()

        val result = state.result
        if (result is DisplayResult.Success) {
            val mimeType = result.imageInfo.mimeType
            val painter = painterMap[mimeType]
            if (painter != null) {
                val painterSize = painter.intrinsicSize
                translate(
                    left = size.width - painterSize.width - margin.toPx(),
                    top = size.height - painterSize.height - margin.toPx()
                ) {
                    with(painter) {
                        draw(painterSize)
                    }
                }
            }
        }
    }

    fun update(state: AsyncImageState, painterMap: Map<String, Painter>, margin: Dp) {
        this.state = state
        this.painterMap = painterMap
        this.margin = margin
        invalidateDraw()
    }
}