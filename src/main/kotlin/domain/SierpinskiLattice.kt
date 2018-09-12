package domain

/**
 * Created by FERMAT on 3/26/2018.
 */
enum class SierpinskiLattice(override val centerPoint: Int) : Lattice {
    Sierpinski_Gasket_6(10),
    Sierpinski_Gasket_15(10),
    Sierpinski_Gasket_42(10),
    Sierpinski_Gasket_123(10);

    companion object {
        fun from(value: String): SierpinskiLattice {
            return values().find {
                val split = value.split("_")
                it.name.toUpperCase().contains(split.first().toUpperCase()) && it.name.endsWith(split.last())
            }?.let { it } ?: throw IllegalArgumentException("could not find enum for $value")
        }
    }

}
