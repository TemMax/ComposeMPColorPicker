package com.duglasher.colorpicker.harmony

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.duglasher.colorpicker.ColorPickerState
import com.duglasher.colorpicker.HsvColor
import com.duglasher.colorpicker.toDegree
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min

/**
 *
 * Will show a brightness bar if [showBrightnessBar] is true
 * otherwise all colors are given the provided brightness value
 */
@Composable
public fun HarmonyColorPicker(
    modifier: Modifier = Modifier,
    state: ColorPickerState,
) {
    BoxWithConstraints(modifier) {
        Column(
            Modifier
                .padding(16.dp)
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            val updatedColor by state.color
            val harmonyMode by state.colorHarmonyMode
            val showBrightnessBar by state.showBrightnessBar

            HarmonyColorPickerWithMagnifiers(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.8f),
                hsvColor = updatedColor,
                onColorChanged = {
                    state.selectColor(it)
                },
                harmonyMode = harmonyMode
            )

            if (showBrightnessBar) {
                BrightnessBar(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .weight(0.2f),
                    onValueChange = { value ->
                        state.selectColor(updatedColor.copy(value = value))
                    },
                    currentColor = updatedColor
                )
            }
        }
    }
}

@Composable
private fun HarmonyColorPickerWithMagnifiers(
    modifier: Modifier = Modifier,
    hsvColor: HsvColor,
    onColorChanged: (HsvColor) -> Unit,
    harmonyMode: ColorHarmonyMode
) {
    val hsvColorUpdated by rememberUpdatedState(hsvColor)
    BoxWithConstraints(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp)
            .wrapContentSize()
            .aspectRatio(1f, matchHeightConstraintsFirst = true)

    ) {
        val updatedOnColorChanged by rememberUpdatedState(onColorChanged)
        val diameterPx by remember(constraints.maxWidth) {
            mutableStateOf(constraints.maxWidth)
        }

        var animateChanges by remember {
            mutableStateOf(false)
        }
        var currentlyChangingInput by remember {
            mutableStateOf(false)
        }

        fun updateColorWheel(newPosition: Offset, animate: Boolean) {
            // Work out if the new position is inside the circle we are drawing, and has a
            // valid color associated to it. If not, keep the current position
            val newColor = colorForPosition(
                newPosition,
                IntSize(diameterPx, diameterPx),
                hsvColorUpdated.value
            )
            if (newColor != null) {
                animateChanges = animate
                updatedOnColorChanged(newColor)
            }
        }

        val inputModifier = Modifier.pointerInput(diameterPx) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                currentlyChangingInput = true
                updateColorWheel(down.position, animate = true)
                drag(down.id) { change ->
                    updateColorWheel(change.position, animate = false)
                    change.consume()
                }
                currentlyChangingInput = false
            }
        }

        Box(inputModifier.fillMaxSize()) {
            ColorWheel(
                hsvColor = hsvColor,
                diameter = diameterPx
            )
            HarmonyColorMagnifiers(
                diameterPx = diameterPx,
                hsvColor = hsvColor,
                animateChanges = animateChanges,
                currentlyChangingInput = currentlyChangingInput,
                harmonyMode = harmonyMode
            )
        }
    }
}

private fun colorForPosition(position: Offset, size: IntSize, value: Float): HsvColor? {
    val centerX: Double = size.width / 2.0
    val centerY: Double = size.height / 2.0
    val radius: Double = min(centerX, centerY)
    val xOffset: Double = position.x - centerX
    val yOffset: Double = position.y - centerY
    val centerOffset = hypot(xOffset, yOffset)
    val rawAngle = atan2(yOffset, xOffset).toDegree()
    val centerAngle = (rawAngle + 360.0) % 360.0
    return if (centerOffset <= radius) {
        HsvColor(
            hue = centerAngle.toFloat(),
            saturation = (centerOffset / radius).toFloat(),
            value = value,
            alpha = 1.0f
        )
    } else {
        null
    }
}
