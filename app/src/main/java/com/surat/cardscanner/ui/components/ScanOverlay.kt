package com.surat.cardscanner.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.surat.cardscanner.ui.theme.OverlayDark
import com.surat.cardscanner.ui.theme.ScannerPrimary

@Composable
internal fun ScanOverlay(
    modifier: Modifier = Modifier,
    isScanning: Boolean = true,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan_line")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = CubicBezierEasing(0.45f, 0f, 0.55f, 1f)),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scan_line_y",
    )
    val cornerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corner_alpha"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cardW = size.width * 0.88f
            val cardH = cardW / 1.586f
            val left = (size.width - cardW) / 2f
            val top = (size.height - cardH) / 2f
            val rect = Rect(left, top, left + cardW, top + cardH)
            val cornerR = 12.dp.toPx()

            drawDimOverlay(rect, cornerR)
            drawCardFrame(rect, ScannerPrimary, cornerAlpha, cornerR)
            if (isScanning) drawScanLine(rect, scanLineY, ScannerPrimary)
        }
    }
}

private fun DrawScope.drawDimOverlay(cardRect: Rect, cornerR: Float) {
    val path = Path().apply {
        addRect(Rect(0f, 0f, size.width, size.height))
        addRoundRect(
            RoundRect(
                left = cardRect.left,
                top = cardRect.top,
                right = cardRect.right,
                bottom = cardRect.bottom,
                cornerRadius = CornerRadius(cornerR),
            )
        )
        fillType = PathFillType.EvenOdd
    }
    drawPath(path, OverlayDark)
}

private fun DrawScope.drawCardFrame(rect: Rect, color: Color, cornerAlpha: Float, cornerR: Float) {
    val cornerL = rect.width * 0.15f
    val cornerC = color.copy(alpha = cornerAlpha)

    drawRoundRect(
        color = color.copy(alpha = 0.3f),
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = CornerRadius(cornerR),
        style = Stroke(width = 2f),
    )

    drawCorner(rect.left, rect.top, cornerL, cornerC, Corner.TOP_LEFT, cornerR)
    drawCorner(rect.right, rect.top, cornerL, cornerC, Corner.TOP_RIGHT, cornerR)
    drawCorner(rect.left, rect.bottom, cornerL, cornerC, Corner.BOTTOM_LEFT, cornerR)
    drawCorner(rect.right, rect.bottom, cornerL, cornerC, Corner.BOTTOM_RIGHT, cornerR)
}

private enum class Corner { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

private fun DrawScope.drawCorner(
    x: Float,
    y: Float,
    len: Float,
    color: Color,
    corner: Corner,
    r: Float,
) {
    val path = Path()
    when (corner) {
        Corner.TOP_LEFT -> {
            path.moveTo(x + r + len, y)
            path.lineTo(x + r, y)
            path.arcTo(Rect(x, y, x + 2 * r, y + 2 * r), 270f, -90f, false)
            path.lineTo(x, y + r + len)
        }

        Corner.TOP_RIGHT -> {
            path.moveTo(x - r - len, y)
            path.lineTo(x - r, y)
            path.arcTo(Rect(x - 2 * r, y, x, y + 2 * r), 270f, 90f, false)
            path.lineTo(x, y + r + len)
        }

        Corner.BOTTOM_LEFT -> {
            path.moveTo(x, y - r - len)
            path.lineTo(x, y - r)
            path.arcTo(Rect(x, y - 2 * r, x + 2 * r, y), 180f, -90f, false)
            path.lineTo(x + r + len, y)
        }

        Corner.BOTTOM_RIGHT -> {
            path.moveTo(x, y - r - len)
            path.lineTo(x, y - r)
            path.arcTo(Rect(x - 2 * r, y - 2 * r, x, y), 0f, 90f, false)
            path.lineTo(x - r - len, y)
        }
    }
    drawPath(path, color, style = Stroke(width = 6f, cap = StrokeCap.Round))
}

private fun DrawScope.drawScanLine(rect: Rect, progress: Float, color: Color) {
    val y = rect.top + rect.height * progress
    val glowH = rect.height * 0.08f
    val glowTop = (y - glowH).coerceAtLeast(rect.top)

    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, color.copy(alpha = 0.12f)),
            startY = glowTop,
            endY = y,
        ),
        topLeft = Offset(rect.left, glowTop),
        size = Size(rect.width, y - glowTop),
    )

    drawLine(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                color.copy(alpha = 0.9f),
                color,
                color.copy(alpha = 0.9f),
                Color.Transparent,
            ),
            startX = rect.left,
            endX = rect.right,
        ),
        start = Offset(rect.left, y),
        end = Offset(rect.right, y),
        strokeWidth = 3f,
    )
}
