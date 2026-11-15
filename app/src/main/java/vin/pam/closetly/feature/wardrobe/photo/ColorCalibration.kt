package vin.pam.closetly.feature.wardrobe.photo

import kotlin.math.pow
import kotlin.math.roundToInt
import kotlinx.serialization.Serializable

@Serializable
data class WhiteBalance(val red: Double = 1.0, val green: Double = 1.0, val blue: Double = 1.0) {
    companion object {
        /** White/gray reference under the same lighting as the garment. Preserve its luminance. */
        fun fromNeutralSample(red: Int, green: Int, blue: Int): WhiteBalance {
            require(minOf(red, green, blue) >= 24 && maxOf(red, green, blue) < 250) {
                "Pilih area putih/abu yang tidak terlalu gelap atau terlalu terang."
            }
            val r = linear(red)
            val g = linear(green)
            val b = linear(blue)
            val target = 0.2126 * r + 0.7152 * g + 0.0722 * b
            val gains = WhiteBalance(target / r, target / g, target / b)
            require(listOf(gains.red, gains.green, gains.blue).all { it in 0.35..2.85 }) {
                "Sampel terlalu berwarna. Gunakan kartu abu atau area putih netral pada foto."
            }
            return gains
        }
    }

    fun apply(argb: Int): Int {
        val alpha = argb ushr 24
        val r = encode(linear((argb ushr 16) and 255) * red)
        val g = encode(linear((argb ushr 8) and 255) * green)
        val b = encode(linear(argb and 255) * blue)
        return (alpha shl 24) or (r shl 16) or (g shl 8) or b
    }
}

private fun linear(channel: Int): Double {
    val value = channel / 255.0
    return if (value <= 0.04045) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
}

private fun encode(value: Double): Int {
    val bounded = value.coerceIn(0.0, 1.0)
    val encoded = if (bounded <= 0.0031308) bounded * 12.92 else 1.055 * bounded.pow(1.0 / 2.4) - 0.055
    return (encoded * 255).roundToInt().coerceIn(0, 255)
}
