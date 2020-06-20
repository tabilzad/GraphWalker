package com.montecarlo.domain

import com.montecarlo.lattice.Lattice

data class SimConfig(
        val lattice: Lattice,
        val walker_amount: Walkers
)

enum class Walkers {
    One_Walker,
    One_Walker_Fast,
    Two_Walkers,
    Two_WalkersNoTrap;
}