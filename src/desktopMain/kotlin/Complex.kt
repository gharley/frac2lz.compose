import kotlin.math.sqrt

/***
 * A class for performing complex number math
 *
 */
class Complex(val real: Double, val imaginary: Double) {
    operator fun plus(other: Double): Complex = Complex(real + other, imaginary)
    operator fun plus(other: Complex): Complex = Complex(real + other.real, imaginary + other.imaginary)

    operator fun minus(other: Double): Complex = Complex(real - other, imaginary)
    operator fun minus(other: Complex): Complex = Complex(real - other.real, imaginary - other.imaginary)

    operator fun times(other: Double): Complex = Complex(real * other, imaginary * other)
    operator fun times(other: Complex): Complex =
        Complex(
            (real * other.real - imaginary * other.imaginary),
            (imaginary * other.real + real * other.imaginary)
        )

    operator fun div(other: Double): Complex = Complex(real / other, imaginary / other)
    operator fun div(other: Complex): Complex {
        val divisor = other.real * other.real + other.imaginary * other.imaginary

        return this * Complex(other.real / divisor, -other.imaginary / divisor)
    }

    fun sidesSquared(): Double {
        return real * real + imaginary * imaginary
    }

    fun hypotenuse(): Double {
        return hypotenuse(sidesSquared())
    }

    private fun hypotenuse(sidesSquared: Double): Double {
        return sqrt(sidesSquared)
    }
}
