package com.duglasher.colorpicker

/**
 * A representation of Color in Hue, Saturation and Value form.
 */
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.graphics.Color
import com.duglasher.colorpicker.harmony.ColorHarmonyMode
import com.github.ajalt.colormath.model.HSV
import com.github.ajalt.colormath.model.RGB

/**
 * A representation of Color in Hue, Saturation and Value form.
 */
public data class HsvColor(

    // from = 0.0, to = 360.0
    val hue: Float,

    // from = 0.0, to = 1.0
    val saturation: Float,

    // from = 0.0, to = 1.0
    val value: Float,

    // from = 0.0, to = 1.0
    val alpha: Float
) {

    public fun toColor(): Color {
        val hsv = HSV(hue, saturation, value, alpha)
        val rgb = hsv.toSRGB()
        return Color(rgb.redInt, rgb.greenInt, rgb.blueInt, rgb.alphaInt)
    }

    public fun getComplementaryColor(): List<HsvColor> {
        return listOf(
            this.copy(saturation = (saturation + 0.1f).coerceAtMost(1f), value = (value + 0.3f).coerceIn(0.0f, 1f)),
            this.copy(saturation = (saturation - 0.1f).coerceAtMost(1f), value = (value - 0.3f).coerceIn(0.0f, 1f)),
            this.copy(hue = (hue + 180) % 360), // actual complementary
            this.copy(hue = (hue + 180) % 360, saturation = (saturation + 0.2f).coerceAtMost(1f), value = (value - 0.3f).coerceIn(0.0f, 1f))
        )
    }

    public fun getSplitComplementaryColors(): List<HsvColor> {
        return listOf(
            this.copy(hue = (hue + 150) % 360, saturation = (saturation - 0.05f).coerceIn(0.0f, 1f), value = (value - 0.3f).coerceIn(0.0f, 1f)),
            this.copy(hue = (hue + 210) % 360, saturation = (saturation - 0.05f).coerceIn(0.0f, 1f), value = (value - 0.3f).coerceIn(0.0f, 1f)),
            this.copy(hue = (hue + 150) % 360), // actual
            this.copy(hue = (hue + 210) % 360) // actual
        )
    }

    public fun getTriadicColors(): List<HsvColor> {
        return listOf(
            this.copy(hue = (hue + 120) % 360, saturation = (saturation - 0.05f).coerceIn(0.0f, 1f), value = (value - 0.3f).coerceIn(0.0f, 1f)),
            this.copy(hue = (hue + 120) % 360),
            this.copy(hue = (hue + 240) % 360, saturation = (saturation - 0.05f).coerceIn(0.0f, 1f), value = (value - 0.3f).coerceIn(0.0f, 1f)),
            this.copy(hue = (hue + 240) % 360)
        )
    }

    public fun getTetradicColors(): List<HsvColor> {
        return listOf(
            this.copy(saturation = (saturation + 0.2f).coerceIn(0.0f, 1f)), // bonus one
            this.copy(hue = (hue + 90) % 360),
            this.copy(hue = (hue + 180) % 360),
            this.copy(hue = (hue + 270) % 360)
        )
    }

    public fun getAnalagousColors(): List<HsvColor> {
        return listOf(
            this.copy(hue = (hue + 30) % 360),
            this.copy(hue = (hue + 60) % 360),
            this.copy(hue = (hue + 90) % 360),
            this.copy(hue = (hue + 120) % 360)
        )
    }

    public fun getMonochromaticColors(): List<HsvColor> {
        return listOf(
            this.copy(saturation = (saturation + 0.2f).mod(1f)),
            this.copy(saturation = (saturation + 0.4f).mod(1f)),
            this.copy(saturation = (saturation + 0.6f).mod(1f)),
            this.copy(saturation = (saturation + 0.8f).mod(1f))
        )
    }

    public fun getShadeColors(): List<HsvColor> {
        return listOf(
            this.copy(value = (value - 0.10f).mod(1.0f).coerceAtLeast(0.2f)),
            this.copy(value = (value + 0.55f).mod(1.0f).coerceAtLeast(0.55f)),
            this.copy(value = (value + 0.30f).mod(1.0f).coerceAtLeast(0.3f)),
            this.copy(value = (value + 0.05f).mod(1.0f).coerceAtLeast(0.2f))
        )
    }

    public fun getColors(colorHarmonyMode: ColorHarmonyMode): List<HsvColor> {
        return when (colorHarmonyMode) {
            ColorHarmonyMode.NONE          -> emptyList()
            ColorHarmonyMode.COMPLEMENTARY -> getComplementaryColor()
            ColorHarmonyMode.ANALOGOUS -> getAnalagousColors()
            ColorHarmonyMode.SPLIT_COMPLEMENTARY -> getSplitComplementaryColors()
            ColorHarmonyMode.TRIADIC -> getTriadicColors()
            ColorHarmonyMode.TETRADIC -> getTetradicColors()
            ColorHarmonyMode.MONOCHROMATIC -> getMonochromaticColors()
            ColorHarmonyMode.SHADES -> getShadeColors()
        }
    }

    public companion object {

        public val DEFAULT: HsvColor = HsvColor(360f, 1.0f, 1.0f, 1.0f)

        /**
         *  the color math hsv to local hsv color
         */
        private fun HSV.toColor(): HsvColor {
            return HsvColor(
                hue = if (this.h.isNaN()) 0f else this.h,
                saturation = this.s,
                value = this.v,
                alpha = this.alpha
            )
        }

        public fun from(color: Color): HsvColor {
            return RGB(
                color.red,
                color.green,
                color.blue,
                color.alpha
            ).toHSV().toColor()
        }

        public val Saver: Saver<HsvColor, *> = listSaver(
            save = {
                listOf(
                    it.hue,
                    it.saturation,
                    it.value,
                    it.alpha
                )
            },
            restore = {
                HsvColor(
                    it[0],
                    it[1],
                    it[2],
                    it[3]
                )
            }
        )
    }
}