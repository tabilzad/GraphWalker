package services

import com.google.common.base.Function
import domain.Lattice
import edu.uci.ics.jung.algorithms.layout.KKLayout
import edu.uci.ics.jung.graph.Graph
import edu.uci.ics.jung.visualization.VisualizationImageServer
import edu.uci.ics.jung.visualization.decorators.EdgeShape
import edu.uci.ics.jung.visualization.renderers.Renderer
import flip
import graphs
import makeWordDocument
import java.awt.*
import java.awt.geom.Ellipse2D
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JLabel

class MarkovChains(
        override val lattice: Lattice
) : VirtualNode(lattice) {

    fun calculateCombinations() {
        print("CalculatingPairs...")
        val graph: Graph<Number, Number> = graphs[lattice]!!

        val allPairs = graph.vertices.findAllPairs()

        val mat = graph.vertices.windowed(3, 3).map {
            it.map {
                it.toInt()
            }.toIntArray()
        }

        // val myMatrix = Array(3) { IntArray(3) }
        val reflections = mutableListOf<Pair<Pair<Number, Number>, Pair<Number, Number>>>()

        allPairs.forEach { initialPair ->
            (0..2).fold(mat) { a, b ->
                val new = rotateMatrix(a)
                val coords = initialPair.getIndexIn(new)
                reflections.add(initialPair to (mat[coords.first.first][coords.first.second] to mat[coords.second.first][coords.second.second]))
                // reflections.add(mat[x][y] to new[x][y])
                new
            }
        }

        val map = reflections.groupBy { it.first }.toMutableMap()

        // allPairs.fold(allPairs) { a, b ->
        //   val e = map[b]!!.map { it.second }
        // a.minus(e)
        //}

        allPairs.forEach { pair ->
            map[pair]?.let {
                it.map { it.second }
            }?.forEach {
                map.run {
                    remove(it)
                    remove(it.flip())
                }
            }
        }

      //  map.keys

        print(map.keys.size)

        // val combos = map.keys.map { pair -> used to be this
        val combos = map.keys.map { pair ->
            pair to mutableListOf<Pair<Number, Number>>().run {
                add(pair)
                val neighborsA = graph.getNeighbors(pair.first).toMutableList()
                neighborsA.addVirtualSites(pair.first).forEach { a ->
                    val neighborsB = graph.getNeighbors(pair.second).toMutableList()
                    neighborsB.addVirtualSites(pair.second).forEach { b ->
                        if (a == pair.second && b == pair.first) {
                            add(a to a)
                        } else add(a to b)
                    }
                }
                this
            }
        }.associateBy({ it.first }, { it.second })

        //(6 to 6).giveReflections(reflections)
        graph.renderGraph(combos)
    }

    fun Pair<Number, Number>.getIndexIn(mat: List<IntArray>): Pair<Pair<Int, Int>, Pair<Int, Int>> {
        var coord: Pair<Int, Int> = -1 to -1
        var coord2: Pair<Int, Int> = -1 to -1


        mat.forEachIndexed { x, e ->
            e.forEachIndexed { y, e2 ->
                when (first == second) {
                    true -> when (first) {
                        mat[x][y] -> {
                            coord = x to y
                            coord2 = x to y
                        }
                    }
                    else -> when {
                        (mat[x][y] == first) -> coord = x to y
                        (mat[x][y] == second) -> coord2 = x to y
                    }

                }

            }
        }
        return coord to coord2
    }

    fun Pair<Number, Number>.giveReflections(reflections: List<Pair<Number, Number>>): List<Pair<Number, Number>> {

        val groups = reflections.groupBy { it.first }

        val one = groups[first]!!.map { it.second }
        val two = groups[second]!!.map { it.second }

        return one.plus(two).toMutableList().findAllPairs().distinct()
    } // not needed?


    fun rotateMatrix(list: List<IntArray>): List<IntArray> {
        val mat = list.map { it.map { it }.toIntArray() }
        val N = list.size
        for (x in 0 until N / 2) {
            // Consider elements in group of 4 in
            // current square
            for (y in x until N - x - 1) {
                // store current cell in temp variable
                val temp = mat[x][y]

                // move values from right to top
                mat[x][y] = mat[y][N - 1 - x]

                // move values from bottom to right
                mat[y][N - 1 - x] = mat[N - 1 - x][N - 1 - y]

                // move values from left to bottom
                mat[N - 1 - x][N - 1 - y] = mat[N - 1 - y][x]

                // assign temp to left
                mat[N - 1 - y][x] = temp
            }
        }
        return mat.toList()
    }


    fun Graph<Number, Number>.renderGraph(pairs: Map<Pair<Number, Number>, MutableList<Pair<Number, Number>>>) {
        print("Rendering...")
        //val pairs = vertices.findAllPairs()
        //val layout = FRLayout(this)
        val images = mutableListOf<BufferedImage>()
        val layout = KKLayout(this).apply {
            size = Dimension(1024, 1024)
        }

        //val toSave = PersistentLayoutImpl<Number, Number>(layout)
        //toSave.persist("SquarePlanar3x3Layout")
        VisualizationImageServer(layout, layout.size).run {
            background = Color.WHITE
            renderer.vertexLabelRenderer.position = Renderer.VertexLabel.Position.CNTR
            renderContext.run {
                vertexFontTransformer = Function { Font("Serif", Font.BOLD, 36) }
                setVertexLabelTransformer { vertex ->
                    vertex.toString()
                }
                multiLayerTransformer.apply {
                    // getTransformer(Layer.LAYOUT).rotate(Math.PI/2, Point2D.Double(width/2.0, height/2.0))
                }
                edgeShapeTransformer = EdgeShape.line(this@renderGraph)
                setVertexShapeTransformer {
                    Ellipse2D.Double().apply {
                        width = 60.0
                        height = 60.0
                        x -= width / 2
                        y -= height / 2
                    }

                }
            }
            pairs.forEach { key, combos ->
                combos.forEachIndexed { index, pair ->
                    val jLabel = JLabel().apply {
                        font = Font("Serif", Font.BOLD, 35)
                        text = when (index) {
                            0 -> "Binary Configuration: $key"
                            else -> "Pair $index: $pair"
                        }
                        isOpaque = true
                        background = Color.WHITE
                        setBounds(20, 10, 500, 50)
                    }
                    renderContext.let { rc ->
                        background = Color.WHITE
                        renderer.vertexLabelRenderer.position = Renderer.VertexLabel.Position.CNTR
                        rc.vertexFontTransformer = Function { Font("Serif", Font.BOLD, 36) }
                        rc.setVertexLabelTransformer { vertex ->
                            vertex.toString()
                        }
                        rc.multiLayerTransformer.apply {
                            // getTransformer(Layer.LAYOUT).rotate(Math.PI/2, Point2D.Double(width/2.0, height/2.0))
                        }
                        rc.edgeShapeTransformer = EdgeShape.line(this@renderGraph)
                        rc.setVertexFillPaintTransformer { vertex ->
                            when (vertex) {
                                pair.first -> Color.RED
                                pair.second -> Color.GREEN
                                else -> Color.LIGHT_GRAY
                            }
                        }
                        rc.setVertexStrokeTransformer { vertex ->
                            when ((vertex == pair.first || vertex == pair.second) && pair.first == pair.second) {
                                true -> BasicStroke(10f)
                                else -> BasicStroke(2f)
                            }
                        }
                        rc.setVertexDrawPaintTransformer { vertex ->
                            when ((vertex == pair.first || vertex == pair.second) && pair.first == pair.second) {
                                true -> Color.GREEN
                                else -> Color.BLACK
                            }
                        }
                        rc.setVertexShapeTransformer {
                            Ellipse2D.Double().apply {
                                width = 60.0
                                height = 60.0
                                x -= width / 2
                                y -= height / 2
                            }

                        }
                        add(jLabel)
                    }
                    // showJFrame(this)
                    //saveImage(key = key, pair = pair, idx = index)
                    images.add(getImage(Point2D.Double(graphLayout.size.width / 2.0, graphLayout.size.width / 2.0), Dimension(720, 720)) as BufferedImage)
                    remove(jLabel)
                }
            }
        }
        println("Done!")
        makeWordDocument(images)
    }

    private fun showJFrame(component: VisualizationImageServer<Number, Number>) {
        JFrame("Simple Graph View").let { f ->
            f.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            f.contentPane.add(component)
            f.pack()
            f.isVisible = true
        }
    }

}