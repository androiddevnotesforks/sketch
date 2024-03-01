package com.github.panpf.sketch.util

import com.github.panpf.sketch.resize.internal.ResizeMapping
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.Transparency
import java.awt.geom.AffineTransform
import java.awt.geom.Area
import java.awt.geom.Rectangle2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage


fun BufferedImage.scaled(scaleFactor: Float): BufferedImage {
    val oldWidth = width
    val oldHeight = height
    val newWidth = (oldWidth * scaleFactor).toInt()
    val newHeight = (oldHeight * scaleFactor).toInt()
    val newType = colorModel.transparency
    val newImage = BufferedImage(
        /* width = */ newWidth,
        /* height = */ newHeight,
        /* imageType = */ newType
    )
    val graphics: Graphics2D = newImage.createGraphics()
//        graphics.setRenderingHint(
//            RenderingHints.KEY_INTERPOLATION,
//            RenderingHints.VALUE_INTERPOLATION_BILINEAR
//        )
    graphics.drawImage(
        /* img = */ this,
        /* dx1 = */ 0,
        /* dy1 = */ 0,
        /* dx2 = */ newWidth,
        /* dy2 = */ newHeight,
        /* sx1 = */ 0,
        /* sy1 = */ 0,
        /* sx2 = */ oldWidth,
        /* sy2 = */ oldHeight,
        /* observer = */ null
    )
    graphics.dispose()
    return newImage
}

fun BufferedImage.mapping(mapping: ResizeMapping): BufferedImage {
    val newWidth = mapping.newWidth
    val newHeight = mapping.newHeight
    val newType = colorModel.transparency
    val newImage = BufferedImage(
        /* width = */ newWidth,
        /* height = */ newHeight,
        /* imageType = */ newType
    )
    val graphics: Graphics2D = newImage.createGraphics()
//        graphics.setRenderingHint(
//            RenderingHints.KEY_INTERPOLATION,
//            RenderingHints.VALUE_INTERPOLATION_BILINEAR
//        )
    graphics.drawImage(
        /* img = */ this,
        /* dx1 = */ mapping.destRect.left,
        /* dy1 = */ mapping.destRect.top,
        /* dx2 = */ mapping.destRect.right,
        /* dy2 = */ mapping.destRect.bottom,
        /* sx1 = */ mapping.srcRect.left,
        /* sy1 = */ mapping.srcRect.top,
        /* sx2 = */ mapping.srcRect.right,
        /* sy2 = */ mapping.srcRect.bottom,
        /* observer = */ null
    )
    graphics.dispose()
    return newImage
}

fun BufferedImage.rotated(degree: Int): BufferedImage {
    val source = this
    val sourceSize = Size(source.width, source.height)
    val newSize = sourceSize.rotate(degree)
    val newImage = BufferedImage(newSize.width, newSize.height, source.type)
    val graphics: Graphics2D = newImage.createGraphics()
//        graphics.setRenderingHint(
//            RenderingHints.KEY_INTERPOLATION,
//            RenderingHints.VALUE_INTERPOLATION_BILINEAR
//        )
    graphics.translate(
        /* tx = */ (newSize.width - sourceSize.width) / 2.0,
        /* ty = */ (newSize.height - sourceSize.height) / 2.0
    )
    graphics.rotate(
        /* theta = */ Math.toRadians(degree.toDouble()),
        /* x = */ (sourceSize.width / 2).toDouble(),
        /* y = */ (sourceSize.height / 2).toDouble()
    )
    graphics.drawImage(source, 0, 0, null)
    graphics.dispose()
    return newImage
}

fun BufferedImage.flipped(horizontal: Boolean): BufferedImage {
    val source = this
    val flipped = BufferedImage(source.width, source.height, source.type)
    val graphics = flipped.createGraphics()
    val transform = if (horizontal) {
        AffineTransform.getTranslateInstance(source.width.toDouble(), 0.0)
    } else {
        AffineTransform.getTranslateInstance(0.0, source.height.toDouble())
    }.apply {
        val flip = if (horizontal) {
            AffineTransform.getScaleInstance(-1.0, 1.0)
        } else {
            AffineTransform.getScaleInstance(1.0, -1.0)
        }
        concatenate(flip)
    }
    graphics.transform = transform
    graphics.drawImage(source, 0, 0, null)
    graphics.dispose()
    return flipped
}

fun BufferedImage.horizontalFlipped(): BufferedImage = flipped(horizontal = true)

fun BufferedImage.verticalFlipped(): BufferedImage = flipped(horizontal = false)

fun BufferedImage.backgrounded(color: Int): BufferedImage {
    val source = this
    val newImage = BufferedImage(source.width, source.height, source.type)
    val graphics = newImage.createGraphics()
    graphics.color = Color(color)
    graphics.fillRect(0, 0, source.width, source.height)
    graphics.color = null
    graphics.drawImage(source, 0, 0, null)
    graphics.dispose()
    return newImage
}

fun BufferedImage.mask(color: Int) {
    val graphics = this@mask.createGraphics()
    val alpha = color ushr 24
    graphics.composite = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha / 255f)
    val withoutAlpha = color and 0x00FFFFFF
    graphics.color = Color(withoutAlpha)
    graphics.fillRect(0, 0, this@mask.width, this@mask.height)
    graphics.dispose()
}

fun BufferedImage.copied(): BufferedImage {
    val newImage = BufferedImage(width, height, type)
    val graphics = newImage.createGraphics()
    graphics.drawImage(this, 0, 0, null)
    graphics.dispose()
    return newImage
}

fun BufferedImage.hasAlpha(): Boolean {
    val height = this.height
    val width = this.width
    var hasAlpha = false
    for (i in 0 until width) {
        for (j in 0 until height) {
            val pixelAlpha = this.getRGB(i, j) shr 24
            if (pixelAlpha in 0..254) {
                hasAlpha = true
                break
            }
        }
    }
    return hasAlpha
}

fun BufferedImage.blur(radius: Int): Boolean {
    val imageWidth = this.width
    val imageHeight = this.height
    val pixels: IntArray = try {
        getPixels()
            .apply { fastGaussianBlur(this, imageWidth, imageHeight, radius) }
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
    this.setRGB(
        /* startX = */ 0,
        /* startY = */ 0,
        /* w = */ imageWidth,
        /* h = */ imageHeight,
        /* rgbArray = */ pixels,
        /* offset = */ 0,
        /* scansize = */ imageWidth
    )
    return true
}

fun BufferedImage.getPixels(region: Rect? = null): IntArray {
    val targetPixels = if (region != null) {
        region.width() * region.height()
    } else {
        width * height
    }
    val pixels = IntArray(targetPixels)
    getRGB(
        /* startX = */ region?.left ?: 0,
        /* startY = */ region?.top ?: 0,
        /* w = */ region?.width() ?: width,
        /* h = */ region?.height() ?: height,
        /* rgbArray = */ pixels,
        /* offset = */ 0,
        /* scansize = */ region?.width() ?: width
    )
    return pixels
}

fun BufferedImage.roundedCornered(cornerRadii: FloatArray): BufferedImage {
    val sourceImage = this
    var newImage = BufferedImage(
        /* width = */ sourceImage.width,
        /* height = */ sourceImage.height,
        /* imageType = */ BufferedImage.TYPE_INT_ARGB
    )
    val shape = Rectangle2D.Double(
        /* x = */ 0.0,
        /* y = */ 0.0,
        /* w = */ sourceImage.width.toDouble(),
        /* h = */ sourceImage.height.toDouble()
    )
    var graphics = newImage.createGraphics()
    newImage = graphics.deviceConfiguration.createCompatibleImage(
        /* width = */ sourceImage.width,
        /* height = */ sourceImage.height,
        /* transparency = */ Transparency.TRANSLUCENT
    )
    graphics = newImage.createGraphics()
    graphics.composite = AlphaComposite.Clear
    graphics.fill(Rectangle(newImage.width, newImage.height))
    graphics.composite = AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f)
    graphics.clip = shape
    graphics = newImage.createGraphics()
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    val roundedCornersShape = Area().apply {
        add(
            Area(
                RoundRectangle2D.Double(
                    /* x = */ 0.0,
                    /* y = */ 0.0,
                    /* w = */ sourceImage.width * 0.75,
                    /* h = */ sourceImage.height * 0.75,
                    /* arcw = */ cornerRadii[0].toDouble(),
                    /* arch = */ cornerRadii[1].toDouble()
                )
            )
        )
        add(
            Area(
                RoundRectangle2D.Double(
                    /* x = */ sourceImage.width * 0.25,
                    /* y = */ 0.0,
                    /* w = */ sourceImage.width * 0.75,
                    /* h = */ sourceImage.height * 0.75,
                    /* arcw = */ cornerRadii[2].toDouble(),
                    /* arch = */ cornerRadii[3].toDouble()
                )
            )
        )
        add(
            Area(
                RoundRectangle2D.Double(
                    /* x = */ sourceImage.width * 0.25,
                    /* y = */ sourceImage.height * 0.25,
                    /* w = */ sourceImage.width * 0.75,
                    /* h = */ sourceImage.height * 0.75,
                    /* arcw = */ cornerRadii[4].toDouble(),
                    /* arch = */ cornerRadii[5].toDouble()
                )
            )
        )
        add(
            Area(
                RoundRectangle2D.Double(
                    /* x = */ 0.0,
                    /* y = */ sourceImage.height * 0.25,
                    /* w = */ sourceImage.width * 0.75,
                    /* h = */ sourceImage.height * 0.75,
                    /* arcw = */ cornerRadii[6].toDouble(),
                    /* arch = */ cornerRadii[7].toDouble()
                )
            )
        )
    }
    graphics.fill(roundedCornersShape)
    graphics.composite = AlphaComposite.SrcIn
    graphics.drawImage(sourceImage, 0, 0, sourceImage.width, sourceImage.height, null)
    graphics.dispose()
    return newImage
}