package services

import domain.*
import domain.lattice.HoneycombLattice
import domain.lattice.SierpinskiLattice
import domain.lattice.SquarePlanarLattice
import domain.lattice.TriangularLattice

/**
 * Created by FERMAT on 4/20/2018.
 */
abstract class VirtualNode(open val lattice: Lattice) {

    fun <T> MutableList<T>.addVirtualSites(virtual: T): MutableList<T> {
        return when (lattice) {
            is SquarePlanarLattice -> addVirtualForSquarePlanar(virtual)
            is HoneycombLattice -> addVirtualForHoneycomb(virtual)
            is TriangularLattice -> addVirtualForTriangular(virtual)
            is SierpinskiLattice -> this
            else -> throw UnsupportedOperationException("This Lattice is not currently supported")
        }
    }

    private fun <T> MutableList<T>.addVirtualForSquarePlanar(virtual: T): MutableList<T> {
        when (this.size) {
            2 -> this.addAll(listOf(virtual, virtual))
            3 -> this.add(virtual)
        }
        return this
    }

    private fun <T> MutableList<T>.addVirtualForTriangular(virtual: T): MutableList<T> = run {
        when (this.size) {
            3 -> this.addAll(listOf(virtual, virtual, virtual))
            4 -> this.addAll(listOf(virtual, virtual))
        }
        this
    }

    private fun <T> MutableList<T>.addVirtualForHoneycomb(virtual: T): MutableList<T> {
        if (this.size == 2) this.add(virtual)
        return this
    }

    fun <T> MutableCollection<T>.findAllPairs(): MutableList<Pair<T, T>> = mutableListOf<Pair<T, T>>().let {
        this.forEachIndexed { index, a ->
            (index + 0 until this.size).mapTo(it) { it -> a to this.toList()[it] }
        }
        it
    }
}