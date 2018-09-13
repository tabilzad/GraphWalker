package domain

/**
 * Created by FERMAT on 3/26/2018.
 */
enum class SierpinskiLattice(override val centerPoint: Int, val trap: Int) : Lattice {
    Sierpinski_Gasket_6(1, 2),
    Sierpinski_Gasket_15(8,14),
    Sierpinski_Gasket_42(22,41),
    Sierpinski_Gasket_123(63,122);

    companion object {
        fun from(value: String): SierpinskiLattice {
            return values().find {
                val split = value.split("_")
                it.name.toUpperCase().contains(split.first().toUpperCase()) && it.name.endsWith(split.last())
            }?.let { it } ?: throw IllegalArgumentException("could not find enum for $value")
        }
    }

}
