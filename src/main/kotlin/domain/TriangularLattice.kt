package domain

/**
* Created by FERMAT on 3/26/2018.
*/
enum class TriangularLattice(override val centerPoint: Int) : Lattice {
    HexagonGrid_19(10),
    HexagonGrid_37(18),
    HexagonGrid_61(30),
    HexagonGrid_91(45),
    HexagonGrid_127(63),
    HexagonGrid_169(68),
    HexagonGrid_217(108);

    fun from(value: String): TriangularLattice {
        return values().find { it.name.contains(value) }
                ?.let {
                    this
                } ?: throw IllegalArgumentException("could not find enum for $value")
    }

}
