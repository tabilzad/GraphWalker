package domain.lattice

import domain.Lattice

/**
 * Created by FERMAT on 3/26/2018.
 */
enum class TowerLattice(override val centerPoint: Int,
                        override val trap: Int = 0,
                        override val trap2: Int = 0) : Lattice {
    Tower_10(4,6,9),
    Tower_34(15, 20, 33),
    Tower_130(60, 72, 129),
    Tower_514(245, 272, 513);

    companion object {
        fun from(value: String): TowerLattice {
            return values().find {
                val split = value.split("_")
                it.name.toUpperCase().contains(split.first().toUpperCase()) && it.name.endsWith(split.last())
            }?.let { it } ?: throw IllegalArgumentException("could not find enum for $value")
        }
    }

}
