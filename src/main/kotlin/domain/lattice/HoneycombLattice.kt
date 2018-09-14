package domain.lattice

import domain.Lattice

/**
 * Created by FERMAT on 3/26/2018.
 */
enum class HoneycombLattice(
        override val centerPoint: Int,
        override val trap: Int = 0,
        override val trap2: Int = 0

) : Lattice {
    FLOWER_24(7),
    FLOWER_54(8),
    FLOWER_96(41),
    FLOWER_150(66),
    FLOWER_216(97);

    fun from(value: String): HoneycombLattice {
        return values().find { it.name.contains(value) }
                ?.let {
                    this
                } ?: throw IllegalArgumentException("could not find enum for $value")
    }

}
