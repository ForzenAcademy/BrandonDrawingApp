package com.example.drawingactivity

import android.graphics.Color

/**
 * A data class that contains color values.
 * Contains: Hue, Saturation, and Value.
 */
data class Hsv(
    var hue: Float,
    var saturation: Float,
    var value: Float
) {

    companion object {
        /**
         * Calculates the Hex of a color using a RGB array.
         */
        fun calculateHex(rgb: IntArray): String {
            return String.format(
                "#%02x%02x%02x",
                rgb[0],
                rgb[1],
                rgb[2]
            )
        }

        /**
         * Generates missing color values based on what is provided.
         * Calculates the RGB of a color using a HSV float array.
         * Returns a Hsv data class if provided an RGB array.
         * Returns an RGB array if provided a HSV array.
         * If invalid color data is provided, throws a Runtime Exception.
         */
        fun toColor(colorData: Any): Any {
            return when (colorData) {
                is Hsv -> {
                    val rgbArray = IntArray(3)
                    val currentColor = Color.HSVToColor(
                        floatArrayOf(colorData.hue, colorData.saturation, colorData.value)
                    )
                    rgbArray[0] = Color.red(currentColor)
                    rgbArray[1] = Color.green(currentColor)
                    rgbArray[2] = Color.blue(currentColor)
                    return rgbArray
                }
                is IntArray -> {
                    val hsv = FloatArray(3)
                    val currentColor = Color.rgb(colorData[0], colorData[1], colorData[2])
                    Color.colorToHSV(currentColor, hsv)
                    return Hsv(
                        hue = hsv[0],
                        saturation = hsv[1],
                        value = hsv[2]
                    )
                }
                else -> {
                    throw RuntimeException("Invalid Color Information Provided.")
                }
            }
        }
    }
}