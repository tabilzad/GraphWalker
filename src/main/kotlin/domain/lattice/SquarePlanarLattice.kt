package domain.lattice

import domain.Lattice

/**
 * Created by FERMAT on 3/26/2018.
 */
enum class SquarePlanarLattice(override val centerPoint: Int,
                               override val trap: Int = 0,
                               override val trap2: Int = 0) : Lattice {
    Square_3(7),
    Square_5(7);

    fun from(value: String): SquarePlanarLattice {
        return values().find { it.name.contains(value) }
                ?.let {
                    this
                } ?: throw IllegalArgumentException("could not find enum for $value")
    }

}
