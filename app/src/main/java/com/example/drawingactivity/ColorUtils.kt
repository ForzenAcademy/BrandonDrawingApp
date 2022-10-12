package com.example.drawingactivity

import java.lang.Integer.parseInt

object ColorUtils {

    /**
     * Function that returns a boolean based on if the string matches a hexadecimal pattern.
     */
    fun isHexadecimal(input: String): Boolean {
        return if (input.contains("#") && input.length == 7) {
            try {
                parseInt(input.substring(1, input.length - 1), 16)
                true
            } catch (e: Exception) {
                false
            }
        } else false
    }
}

object ColorConstants {
    const val hueMax: Float = 360f
}