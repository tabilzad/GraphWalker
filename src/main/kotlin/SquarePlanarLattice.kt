/**
 * Created by FERMAT on 3/26/2018.
 */
enum class SquarePlanarLattice(override val centerPoint: Int) : Lattice {
    Square_3(7),
    Square_5(7);

    fun from(value: String): SquarePlanarLattice {
        return values().find { it.name.contains(value) }
                ?.let {
                    this
                } ?: throw IllegalArgumentException("could not find enum for $value")
    }

}
