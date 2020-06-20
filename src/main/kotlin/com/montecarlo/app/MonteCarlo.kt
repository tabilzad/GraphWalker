package com.montecarlo.app

import SierpinskiLattice
import TowerLattice
import TriangularLattice
import com.montecarlo.app.UI.MyApp
import com.montecarlo.domain.BowtieLattice
import com.montecarlo.lattice.HoneycombLattice
import com.montecarlo.lattice.Lattice
import com.montecarlo.lattice.SquarePlanarLattice
import edu.uci.ics.jung.graph.Graph
import edu.uci.ics.jung.io.graphml.GraphMLReader2
import javafx.application.Application
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader


val threads_count = 48
val probability = 0.0
val Iterations = 80/*_000_000*/ / threads_count
val inputGraph = HoneycombLattice.FLOWER_24
val graphs = loadGraphs(
        "flower_24",
        "flower_54",
        "flower_96",
        "flower_150",
        "flower_216",
        "Bowtie",
        "fast38",
        "HexagonGrid_19",
        "HexagonGrid_37",
        "HexagonGrid_61",
        "HexagonGrid_91",
        "HexagonGrid_127",
        "HexagonGrid_169",
        "HexagonGrid_217",
        "TriangularGrid_1275",
        "testTest2",
        "Square_3",
        "Square_5",
        "Square_6",
        "sierpinski_6",
        "sierpinski_15",
        "sierpinski_42",
        "sierpinski_123",
        "tower_10",
        "tower_34",
        "tower_130",
        "tower_514"
)

fun main(args: Array<String>) {
    //showMemory()
    Application.launch(MyApp::class.java, *args)
}

fun loadGraphs(vararg names: String): Map<Lattice, Graph<Number, Number>> {
    return names.map<String, Pair<Lattice, Graph<Number, Number>>> {
        val basePath = ClassLoader.getSystemResource("Shapes").path
        when {
            it.contains("flower") -> HoneycombLattice.valueOf(it.toUpperCase()) to loadGraphML("$basePath\\Flower\\$it.graphml")
            it.contains("Bowtie") -> BowtieLattice.BOWTIE to loadGraphML("$basePath\\Flower\\BowtieGraph.graphml")
            it.contains("fast38") -> BowtieLattice.BOWTIE_38 to loadGraphML("$basePath\\Flower\\BowtieGraph38.graphml")
            it.contains("HexagonGrid") -> HexagonalLattice.valueOf(it) to loadGraphML("$basePath\\Dual\\$it.graphml")
            it.contains("TriangularGrid") -> TriangularLattice.valueOf(it) to loadGraphML("$basePath\\Tri\\$it.graphml")
            it.contains("testTest2") -> TriangularLattice.valueOf("TriangularGrid_256") to loadGraphML("$basePath\\Tri\\$it.graphml")
            it.contains("sierpinski") -> SierpinskiLattice.from(it) to loadGraphML("$basePath\\Fractal\\$it.graphml")
            it.contains("tower") -> TowerLattice.from(it) to loadGraphML("$basePath\\Fractal\\$it.graphml")
            else -> SquarePlanarLattice.valueOf(it) to loadGraphML("$basePath\\Square\\$it.graphml")
        }
    }.toMap()
}

private fun loadGraphML(s: String): Graph<Number, Number> {
    val stream = FileInputStream(File(s))
    val reader = GraphMLReader2(InputStreamReader(stream), graphFactory(), vertexFactory(), edgeFactory(), hyperEdgeFactory())
    return reader.readGraph()
}