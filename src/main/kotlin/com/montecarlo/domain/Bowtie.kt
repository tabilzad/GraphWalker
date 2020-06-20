package com.montecarlo.domain

import com.montecarlo.lattice.Lattice

enum class BowtieLattice(
        override val trap: Int,
        override val trap2: Int,
        override val centerPoint: Int
) : Lattice {

    BOWTIE(0, 0, 0),
    BOWTIE_38(0, 0, 0);

}