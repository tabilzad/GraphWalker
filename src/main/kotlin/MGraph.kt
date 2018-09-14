import domain.Lattice
import domain.lattice.SierpinskiLattice
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath
import edu.uci.ics.jung.graph.Graph
import services.VirtualNode
import java.util.concurrent.*

class MGraph(
        val Iterations: Int,
        val pb: Double,
        val graphInfo: Pair<Graph<Number, Number>, Lattice>
) : VirtualNode(graphInfo.second) {
    val graph = graphInfo.first
    var list = mutableListOf<Int>()
    var steps = 0
    var walker1: Number = 0
    var walker2: Number = 0

    fun run_sim() {
        graphInfo.second.let { lattice ->
            (1..Iterations).forEach {
                do {
                    walker1 = graph.vertices.toList()[randomize(graph.vertexCount)]
                } while (walker1 == lattice.centerPoint)
                while (walker1 != lattice.centerPoint) {
                    walker1 = walk(walker1)
                    steps++
                }
                list.add(steps)
                steps = 0
            }
        }
    }

    fun run_sierpinski() {
        val lattice = graphInfo.second
        (1..Iterations).forEach {
            do {
                walker1 = graph.vertices.toList()[randomize(graph.vertexCount)]
            } while (walker1 == lattice.centerPoint || walker1 == lattice.trap)

            val to = DijkstraDistance(graph).let {
                val d1 = it.getDistance(walker1, lattice.centerPoint).toInt()
                val d2 = it.getDistance(walker1, lattice.trap).toInt()
                if (d1 < d2) lattice.centerPoint else lattice.trap
            }

            while ((walker1 != lattice.centerPoint) and (walker1 != lattice.trap)) {
                // println("Walker at:$walker1")
                walker1 = walkShortest(walker1, to)
                // println("Walked to:$walker1")
                steps++
            }
            //  println("-----Exited")
            // println("-----Made steps: $steps")
            list.add(steps)
            steps = 0

        }
    }

    fun run_sierpinski3D() {
        val lattice = graphInfo.second
        (1..Iterations).forEach {
            do {
                walker1 = graph.vertices.toList()[randomize(graph.vertexCount)]
            } while (walker1 == lattice.centerPoint || walker1 == lattice.trap || walker1 == lattice.trap2)

            val to = DijkstraDistance(graph).let {
                val d1 = it.getDistance(walker1, lattice.centerPoint).toInt()
                val d2 = it.getDistance(walker1, lattice.trap).toInt()
                if (d1 < d2) lattice.centerPoint else lattice.trap
            }

            while ((walker1 != lattice.centerPoint) and (walker1 != lattice.trap) and (walker1 != lattice.trap2)) {
                // println("Walker at:$walker1")
                walker1 = walkShortest(walker1, to)
                // println("Walked to:$walker1")
                steps++
            }
            //  println("-----Exited")
            // println("-----Made steps: $steps")
            list.add(steps)
            steps = 0

        }
    }


    fun run_simTwoWalker() {
        graphInfo.second.let { lattice ->
            (1..Iterations).forEach {
                do {
                    walker1 = graph.vertices.toList()[randomize(graph.vertexCount)]
                    walker2 = graph.vertices.toList()[randomize(graph.vertexCount)]
                } while (walker1 == walker2)
                while (walker1 != walker2) {

                    val OLD_p = walker1
                    val OLD_p2 = walker2

                    walker1 = walk(walker1)
                    walker2 = walk(walker2)

                    steps++
                    if (walker1 == OLD_p2 && walker2 == OLD_p) {
                        break
                    }
                }
                list.add(steps)
                steps = 0
            }
        }
    }


    fun walk(vertex: Number): Number {
        return graph.getNeighbors(vertex).toMutableList().let { neighbors ->
            neighbors.addVirtualSites(vertex).let { virtual ->
                virtual[randomize(virtual.size)]
            }
        }
    }

    fun walkShortest(from: Number, to: Number): Number {
        val edge = DijkstraShortestPath(graph).getPath(from, to).first()
        return graph.getEndpoints(edge).first { it != from }
    }


    fun prob(probabilityTrue: Double): Boolean = ThreadLocalRandom.current().nextDouble() >= 1.0 - probabilityTrue
    private fun randomize(grid_size: Int): Int = ThreadLocalRandom.current().nextInt(grid_size)
}
