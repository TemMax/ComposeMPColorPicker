package com.duglasher.colorpicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.duglasher.colorpicker.harmony.ColorHarmonyMode

@Stable
public interface ColorPickerState {

    public val color: State<HsvColor>
    public val showAlphaBar: State<Boolean>
    public val showBrightnessBar: State<Boolean>
    public val colorHarmonyMode: State<ColorHarmonyMode>

    public fun selectColor(color: HsvColor)
    public fun showAlphaBar(show: Boolean)
    public fun showBrightnessBar(show: Boolean)
    public fun setColorHarmonyMode(mode: ColorHarmonyMode)

    public companion object {
        public val Saver: Saver<ColorPickerState, *> = mapSaver(
            save = {
                mapOf(
                    "color" to it.color.value.toColor().toArgb(),
                    "showAlphaBar" to it.showAlphaBar.value,
                    "showBrightnessBar" to it.showBrightnessBar.value,
                    "colorHarmonyMode" to it.colorHarmonyMode.value.name
                )
            },
            restore = {
                ColorPickerStateImpl().apply {
                    color.value = HsvColor.from(Color(it["color"] as Int))
                    showAlphaBar.value = it["showAlphaBar"] as Boolean
                    showBrightnessBar.value = it["showBrightnessBar"] as Boolean
                    colorHarmonyMode.value =
                        ColorHarmonyMode.valueOf(it["colorHarmonyMode"] as String)
                }
            }
        )
    }

}

private class ColorPickerStateImpl : ColorPickerState {

    override val color: MutableState<HsvColor> = mutableStateOf(HsvColor.from(Color.Black))
    override val showAlphaBar: MutableState<Boolean> = mutableStateOf(false)
    override val showBrightnessBar: MutableState<Boolean> = mutableStateOf(false)
    override val colorHarmonyMode: MutableState<ColorHarmonyMode> =
        mutableStateOf(ColorHarmonyMode.NONE)

    override fun selectColor(color: HsvColor) {
        this.color.value = color
    }

    override fun showAlphaBar(show: Boolean) {
        showAlphaBar.value = show
    }

    override fun showBrightnessBar(show: Boolean) {
        showBrightnessBar.value = show
    }

    override fun setColorHarmonyMode(mode: ColorHarmonyMode) {
        colorHarmonyMode.value = mode
    }

}

@Composable
public fun rememberColorPickerState(): ColorPickerState {
    return rememberSaveable(saver = ColorPickerState.Saver) {
        ColorPickerStateImpl()
    }
}