@file:Suppress(
    "INVISIBLE_REFERENCE",
    "NOTHING_TO_INLINE"
)
package com.pr0gramm3r101.utils

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedTextFieldTopPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.internal.Strings
import androidx.compose.material3.internal.defaultErrorSemantics
import androidx.compose.material3.internal.getString
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt

data class NavigationItem(
    val name: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector = selectedIcon,
    val onClick: () -> Unit
)

@Composable
fun SimpleNavigationBar(
    modifier: Modifier = Modifier,
    selectedItem: Int,
    vararg items: NavigationItem,
) {
    NavigationBar(modifier) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector =
                            if (selectedItem == index)
                                items[index].selectedIcon
                            else
                                items[index].unselectedIcon,
                        contentDescription = item.name
                    )
                },
                label = { Text(item.name) },
                selected = selectedItem == index,
                onClick = items[index].onClick
            )
        }
    }
}

@Composable
fun SimpleNavigationRail(
    modifier: Modifier = Modifier,
    selectedItem: Int,
    vararg items: NavigationItem
) {
    NavigationRail(
        modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        items.forEachIndexed { index, item ->
            NavigationRailItem(
                icon = {
                    Icon(
                        imageVector =
                            if (selectedItem == index)
                                items[index].selectedIcon
                            else
                                items[index].unselectedIcon,
                        contentDescription = item.name
                    )
                },
                label = { Text(item.name) },
                selected = selectedItem == index,
                onClick = items[index].onClick
            )
        }
    }
}

@Composable
fun AnimatedCrosslineIcon(
    icon: ImageVector,
    crossline: Boolean,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val lineDrawProgress by animateFloatAsState(
        targetValue = if (crossline) 0f else 1f, // 0f for hidden line, 1f for drawn line
        animationSpec = tween(300),
        label = "line_draw_progress"
    )

    val iconColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .size(24.dp)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            },
        contentAlignment = Alignment.Center
    ) {
        val vectorPainter = rememberVectorPainter(icon)

        Canvas(modifier = Modifier.matchParentSize()) {
            val colorFilter = ColorFilter.tint(iconColor)

            // Draw the base Visibility icon directly to the canvas
            drawIntoCanvas {
                with(vectorPainter) {
                    draw(size, 1f, colorFilter)
                }
            }

            // Draw the animating main cross line and its 'background' strip on top
            if (!crossline || lineDrawProgress > 0f) {
                // Coordinates for the diagonal line (from top-left to bottom-right)
                val x1 = size.width * 0.05f
                val y1 = size.height * 0.15f
                val x2 = size.width * 0.825f
                val y2 = size.height * 0.925f

                // Current end point of the drawing line based on animation progress
                val currentX = x1 + (x2 - x1) * lineDrawProgress
                val currentY = y1 + (y2 - y1) * lineDrawProgress

                val mainLineThickness = 2.dp.toPx()
                val backgroundStripThickness = 2.dp.toPx() // Thickness of the 'blank background' strip

                // Calculate perpendicular vector for thickness and offset for the second line
                val dx = x2 - x1
                val dy = y2 - y1
                val length = sqrt(dx * dx + dy * dy)
                // Perpendicular vector normalized (points to the 'left' side relative to line direction)
                val nx = -dy / length
                val ny = dx / length

                // Global offset to shift both lines further to the left
                val globalLineOffset = mainLineThickness / 2

                // --- Draw the 'Blank background' strip path (filled rectangle) first, attached to the left/top-left of the main line ---
                // This path will now erase pixels
                drawPath(
                    path = Path().apply {
                        val offsetFromMainLineCenter = mainLineThickness / 2 + backgroundStripThickness / 2 - 1.dp.toPx()

                        // Points for the strip, offset to the 'top-left' side by subtracting nx and ny
                        // Apply globalLineOffset to shift both lines further left
                        val p1x = x1 - nx * (offsetFromMainLineCenter + globalLineOffset)
                        val p1y = y1 - ny * (offsetFromMainLineCenter + globalLineOffset)
                        val p2x = x1 - nx * (offsetFromMainLineCenter + backgroundStripThickness + globalLineOffset)
                        val p2y = y1 - ny * (offsetFromMainLineCenter + backgroundStripThickness + globalLineOffset)

                        val cp1x = currentX - nx * (offsetFromMainLineCenter + globalLineOffset)
                        val cp1y = currentY - ny * (offsetFromMainLineCenter + globalLineOffset)
                        val cp2x = currentX - nx * (offsetFromMainLineCenter + backgroundStripThickness + globalLineOffset)
                        val cp2y = currentY - ny * (offsetFromMainLineCenter + backgroundStripThickness + globalLineOffset)

                        moveTo(p1x, p1y)
                        lineTo(p2x, p2y)
                        lineTo(cp2x, cp2y)
                        lineTo(cp1x, cp1y)
                        close()
                    },
                    color = Color.Transparent, // This color will be used for blending calculation
                    style = Fill,
                    blendMode = BlendMode.Clear // This makes it erase pixels underneath
                )

                // --- Draw the Main cross line path (filled rectangle) second ---
                drawPath(
                    path = Path().apply {
                        // Apply globalLineOffset to shift both lines further left
                        val p1x = x1 - nx * (mainLineThickness / 2 + globalLineOffset)
                        val p1y = y1 - ny * (mainLineThickness / 2 + globalLineOffset)
                        val p2x = x1 + nx * (mainLineThickness / 2 - globalLineOffset)
                        val p2y = y1 + ny * (mainLineThickness / 2 - globalLineOffset)

                        val cp1x = currentX - nx * (mainLineThickness / 2 + globalLineOffset)
                        val cp1y = currentY - ny * (mainLineThickness / 2 + globalLineOffset)
                        val cp2x = currentX + nx * (mainLineThickness / 2 - globalLineOffset)
                        val cp2y = currentY + ny * (mainLineThickness / 2 - globalLineOffset)

                        moveTo(p1x, p1y)
                        lineTo(p2x, p2y)
                        lineTo(cp2x, cp2y)
                        lineTo(cp1x, cp1y)
                        close()
                    },
                    color = iconColor,
                    style = Fill
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TweakedOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    // If color is not provided via the text style, use content color as a default
    val textColor =
        textStyle.color.takeOrElse {
            val focused = interactionSource.collectIsFocusedAsState().value
            colors.textColor(enabled, isError, focused)
        }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    val density = LocalDensity.current

    CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
        BasicTextField(
            value = value,
            modifier =
                modifier
                    .then(
                        if (label != null) {
                            Modifier
                                // Merge semantics at the beginning of the modifier chain to ensure
                                // padding is considered part of the text field.
                                .semantics(mergeDescendants = true) {}
                                .padding(top = with(density) { OutlinedTextFieldTopPadding.toDp() })
                        } else {
                            Modifier
                        }
                    )
                    .defaultErrorSemantics(isError, getString(Strings.DefaultErrorMessage))
                    .defaultMinSize(
                        minWidth = OutlinedTextFieldDefaults.MinWidth,
                        minHeight = OutlinedTextFieldDefaults.MinHeight
                    ),
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = mergedTextStyle,
            cursorBrush = SolidColor(colors.cursorColor(isError)),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = value,
                    visualTransformation = visualTransformation,
                    innerTextField = {
                        AnimatedContent(
                            targetState = visualTransformation
                        ) {
                            innerTextField()
                        }
                    },
                    placeholder = placeholder,
                    label = label,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    prefix = prefix,
                    suffix = suffix,
                    supportingText = supportingText,
                    singleLine = singleLine,
                    enabled = enabled,
                    isError = isError,
                    interactionSource = interactionSource,
                    colors = colors,
                    container = {
                        OutlinedTextFieldDefaults.Container(
                            enabled = enabled,
                            isError = isError,
                            interactionSource = interactionSource,
                            colors = colors,
                            shape = shape,
                        )
                    }
                )
            }
        )
    }
}