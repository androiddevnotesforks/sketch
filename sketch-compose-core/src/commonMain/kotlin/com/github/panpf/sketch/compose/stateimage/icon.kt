package com.github.panpf.sketch.compose.stateimage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import com.github.panpf.sketch.compose.painter.rememberIconAnimatablePainter
import com.github.panpf.sketch.compose.painter.rememberIconPainter

@Composable
fun rememberIconPainterStateImage(
    icon: Painter,
    background: Painter? = null,
    iconSize: Size? = null,
    iconTint: Color? = null,
): PainterStateImage {
    val painter = rememberIconPainter(
        icon = icon,
        background = background,
        iconSize = iconSize,
        iconTint = iconTint
    )
    return remember(painter) { PainterStateImage(painter) }
}

@Composable
fun rememberIconPainterStateImage(
    icon: Painter,
    background: Color? = null,
    iconSize: Size? = null,
    iconTint: Color? = null,
): PainterStateImage {
    val painter = rememberIconPainter(
        icon = icon,
        background = background,
        iconSize = iconSize,
        iconTint = iconTint
    )
    return remember(painter) { PainterStateImage(painter) }
}

@Composable
fun rememberIconAnimatablePainterStateImage(
    icon: Painter,
    background: Painter? = null,
    iconSize: Size? = null,
    iconTint: Color? = null,
): PainterStateImage {
    val painter = rememberIconAnimatablePainter(
        icon = icon,
        background = background,
        iconSize = iconSize,
        iconTint = iconTint
    )
    return remember(painter) { PainterStateImage(painter) }
}

@Composable
fun rememberIconAnimatablePainterStateImage(
    icon: Painter,
    background: Color? = null,
    iconSize: Size? = null,
    iconTint: Color? = null,
): PainterStateImage {
    val painter = rememberIconAnimatablePainter(
        icon = icon,
        background = background,
        iconSize = iconSize,
        iconTint = iconTint
    )
    return remember(painter) { PainterStateImage(painter) }
}