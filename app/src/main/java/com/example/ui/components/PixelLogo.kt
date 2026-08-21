package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Bold retro pixel-art renderer for "D agent" with micro pixel "beta" indicator.
 * Enforces LTR layout so it never reverses in RTL/Persian language modes.
 */
@Composable
fun PixelDAgentLogo(
    modifier: Modifier = Modifier,
    pixelSize: Dp = 2.8.dp,
    pixelColor: Color = MaterialTheme.colorScheme.onSurface,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    showBetaBadge: Boolean = true
) {
    // 6x6 high-density bold matrices for bold retro typography
    val glyphD = listOf(
        "111110",
        "110011",
        "110011",
        "110011",
        "110011",
        "111110"
    )

    val glyphA = listOf(
        "011110",
        "000011",
        "011111",
        "110011",
        "011111"
    )

    val glyphG = listOf(
        "011111",
        "110011",
        "011111",
        "000011",
        "111110"
    )

    val glyphE = listOf(
        "011110",
        "110011",
        "111111",
        "110000",
        "011110"
    )

    val glyphN = listOf(
        "110110",
        "111011",
        "110011",
        "110011",
        "110011"
    )

    val glyphT = listOf(
        "01100",
        "11111",
        "01100",
        "01100",
        "00111"
    )

    // Enforce LTR strictly so "D agent" always renders left-to-right correctly in Persian/Arabic RTL
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // 'D' (Accent colored bold pixel letter)
            PixelGlyph(matrix = glyphD, pixelSize = pixelSize, color = accentColor)

            // Space between 'D' and 'agent'
            Canvas(modifier = Modifier.size(width = pixelSize * 2.2f, height = pixelSize * 6)) { }

            // 'a'
            PixelGlyph(matrix = glyphA, pixelSize = pixelSize, color = pixelColor)
            // 'g'
            PixelGlyph(matrix = glyphG, pixelSize = pixelSize, color = pixelColor)
            // 'e'
            PixelGlyph(matrix = glyphE, pixelSize = pixelSize, color = pixelColor)
            // 'n'
            PixelGlyph(matrix = glyphN, pixelSize = pixelSize, color = pixelColor)
            // 't'
            PixelGlyph(matrix = glyphT, pixelSize = pixelSize, color = pixelColor)

            if (showBetaBadge) {
                Spacer(modifier = Modifier.width(pixelSize * 1.8f))
                PixelBetaBadge(
                    pixelSize = (pixelSize * 0.55f).coerceAtLeast(1.2.dp),
                    color = accentColor
                )
            }
        }
    }
}

/**
 * Micro retro pixel art "beta" badge
 */
@Composable
fun PixelBetaBadge(
    modifier: Modifier = Modifier,
    pixelSize: Dp = 1.4.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(4.dp),
            color = color.copy(alpha = 0.12f),
            border = BorderStroke(0.6.dp, color.copy(alpha = 0.35f))
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 3.5.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                PixelBetaText(pixelSize = pixelSize, color = color)
            }
        }
    }
}

/**
 * Renders the word "beta" in micro retro pixel typography
 */
@Composable
fun PixelBetaText(
    modifier: Modifier = Modifier,
    pixelSize: Dp = 1.4.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val glyphBetaB = listOf(
        "1000",
        "1000",
        "1110",
        "1001",
        "1110"
    )
    val glyphBetaE = listOf(
        "0110",
        "1001",
        "1111",
        "1000",
        "0110"
    )
    val glyphBetaT = listOf(
        "010",
        "111",
        "010",
        "010",
        "001"
    )
    val glyphBetaA = listOf(
        "0110",
        "0001",
        "0111",
        "1001",
        "0111"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(pixelSize * 0.8f)
        ) {
            PixelGlyph(matrix = glyphBetaB, pixelSize = pixelSize, color = color, maxRows = 5)
            PixelGlyph(matrix = glyphBetaE, pixelSize = pixelSize, color = color, maxRows = 5)
            PixelGlyph(matrix = glyphBetaT, pixelSize = pixelSize, color = color, maxRows = 5)
            PixelGlyph(matrix = glyphBetaA, pixelSize = pixelSize, color = color, maxRows = 5)
        }
    }
}

@Composable
private fun PixelGlyph(
    matrix: List<String>,
    pixelSize: Dp,
    color: Color,
    maxRows: Int = 6
) {
    val rows = matrix.size
    val cols = matrix.maxOfOrNull { it.length } ?: 0

    Canvas(
        modifier = Modifier.size(
            width = pixelSize * cols,
            height = pixelSize * maxRows
        )
    ) {
        val pxSize = pixelSize.toPx()
        val yOffset = (maxRows - rows) * pxSize

        for (r in matrix.indices) {
            val rowStr = matrix[r]
            for (c in rowStr.indices) {
                if (rowStr[c] == '1') {
                    drawRect(
                        color = color,
                        topLeft = Offset(c * pxSize, yOffset + r * pxSize),
                        size = Size(pxSize * 0.95f, pxSize * 0.95f)
                    )
                }
            }
        }
    }
}
