package com.montecarlo.app.services

import com.google.common.base.Function
import com.montecarlo.lattice.Lattice
import com.montecarlo.domain.MyPersist
import edu.uci.ics.jung.algorithms.layout.FRLayout
import edu.uci.ics.jung.algorithms.layout.KKLayout
import edu.uci.ics.jung.graph.Graph
import edu.uci.ics.jung.visualization.Layer
import edu.uci.ics.jung.visualization.VisualizationImageServer
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.decorators.EdgeShape
import edu.uci.ics.jung.visualization.layout.LayoutTransition
import edu.uci.ics.jung.visualization.renderers.Renderer
import com.montecarlo.app.flip
import com.montecarlo.app.flipMatrixH
import com.montecarlo.app.flipMatrixV
import com.montecarlo.app.getIndexIn
import com.montecarlo.app.graphs
import com.montecarlo.app.inOrder
import com.montecarlo.app.printMatrix
import com.montecarlo.app.rotateMatrix
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.geom.Ellipse2D
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.io.File
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.control.ModalGraphMouse
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin


/**
 * TODO() TASKS:
 * 1st - Just Generate all possible configs for Honeycomb
 * 2nd - Use Kozak's MAPLE file to calc Markov analytic result
 * 3rd - Generate symmetry unique honeycomb
 */


class MarkovChains(
        override val lattice: Lattice
) : VirtualNode(lattice) {

    companion object {
        private val file = File("C:\\Users\\FERMAT\\workspace\\Kotlin\\GraphWalker\\images\\layout.out")
    }

    fun calculateCombinations() {
        print("CalculatingPairs...")
        val graph: Graph<Number, Number> = graphs[lattice]!!

        val allPairs = graph.vertices.findAllPairs()

        //Name this coordinate or indexMap, position Map when comiting
        val matrix = graph.vertices.windowed(3, 3).map { it.map { it.toInt() }.toIntArray() }
        // val myMatrix = Array(3) { IntArray(3) }
        println("*".repeat(20))
        matrix.printMatrix()
        println("*".repeat(20))
        val reflections = mutableListOf<Pair<Pair<Number, Number>, Pair<Number, Number>>>()
        //RotatesMatrix and figures out reflection pairs
        allPairs.forEach { initialPair ->
            (0..2).fold(matrix) { a, b ->
                val new = rotateMatrix(a)
                val coords = initialPair.getIndexIn(new)
                reflections.add(initialPair to (matrix[coords.first.first][coords.first.second] to matrix[coords.second.first][coords.second.second]))
                new
            }
            (0..2).fold(matrix) { a, b ->
                val new = flipMatrixV(rotateMatrix(a))
                val coords = initialPair.getIndexIn(new)
                reflections.add(initialPair to (matrix[coords.first.first][coords.first.second] to matrix[coords.second.first][coords.second.second]))
                new
            }
            (0..2).fold(matrix) { a, b ->
                val new = flipMatrixH(rotateMatrix(a))
                val coords = initialPair.getIndexIn(new)
                reflections.add(initialPair to (matrix[coords.first.first][coords.first.second] to matrix[coords.second.first][coords.second.second]))
                new
            }
            let {
                val new = flipMatrixH(matrix)
                val coords = initialPair.getIndexIn(new)
                reflections.add(initialPair to (matrix[coords.first.first][coords.first.second] to matrix[coords.second.first][coords.second.second]))
            }
            let {
                val new = flipMatrixV(matrix)
                val coords = initialPair.getIndexIn(new)
                reflections.add(initialPair to (matrix[coords.first.first][coords.first.second] to matrix[coords.second.first][coords.second.second]))
            }
        }

        //Re-format reflections
        val map = reflections.groupBy { it.first }.toMutableMap()

        //This filters out symmetry distinct pair

        //optimize
        map.forEach { (key, value) ->
            map[key] = value.toSet().minus(key to key).minus(key to key.flip()).map { it.first to it.second.inOrder() }
        }

        //NEW
        map.keys.fold(map.toMutableMap()) { a, b ->
            a[b]?.let {
                it.forEach {
                    allPairs.remove(it.second)
                }
                a.minus(it.map { it.second }).toMutableMap()
            } ?: a
        }

        print(map.keys.size)

        // val combos = map.keys.map { pair -> used to be this
        //This is always needed
        val combos = allPairs.map { pair ->
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

    fun getGraphViewer() {

        print("GettingViewer...")
        val graph: Graph<Number, Number> = graphs[lattice]!!

        val layout = KKLayout(graph).apply {
            size = Dimension(1024, 1024)
        }
        VisualizationViewer(layout, layout.size).let { vv ->

            vv.run {

                vv.graphMouse = DefaultModalGraphMouse<Int, Int>().apply {

                    this.add(PickingGraphMousePlugin<Int, Int>())
                    setMode(ModalGraphMouse.Mode.PICKING)

                }
                vv.pickedVertexState.addItemListener {


                    println(it.item)
                }
                showJFrame(this)
                background = Color.WHITE
                renderer.vertexLabelRenderer.position = Renderer.VertexLabel.Position.CNTR
                renderContext.let { rc ->
                    rc.setVertexLabelTransformer { vertex ->
                        vertex.toString()
                    }

                    rc.setVertexFillPaintTransformer {

                        when (vv.pickedVertexState.isPicked(it)) {
                            true -> {
                                //vv.pickedVertexState.pick(it, false)
                                Color.blue
                            }
                            false -> {
                                //vv.pickedVertexState.pick(it, true)
                                Color.yellow
                            }

                        }


                    }

                    rc.edgeShapeTransformer = EdgeShape.line(graph)
                    rc.setVertexShapeTransformer {
                        Ellipse2D.Double().apply {
                            width = 40.0
                            height = 40.0
                            x -= width / 2
                            y -= height / 2
                        }


                    }

                }
            }
        }


    }

    //TODO - This is a WIP code
    fun calculateHoneyComb() {

        print("CalculatingPairs...")
        val graph: Graph<Number, Number> = graphs[lattice]!!

        val allPairs = graph.vertices.findAllPairs()
        val layout = KKLayout(graph).apply {
            size = Dimension(1024, 1024)
        }
        val p = MyPersist(layout)
        p.restore(file.absolutePath)
        val vv = VisualizationImageServer(p, p.size)
        val oldCoords = graph.vertices.map { node ->
            layout.let { l -> (l.getX(node) to l.getY(node)) to node }
        }.toMap()
        vv.run {
            background = Color.WHITE
            renderer.vertexLabelRenderer.position = Renderer.VertexLabel.Position.CNTR


            renderContext.run {
                allPairs.forEach { initialPair ->
                    (0..2).forEach {
                        multiLayerTransformer.getTransformer(Layer.LAYOUT).rotate(Math.PI / 2, Point2D.Double(layout.getX(4), layout.getY(4)))
                        /*reflections.add(initialPair to
                                (oldCoords[vv.layout.getX(initialPair.first) to layout.getY(initialPair.first)]!! to
                                 oldCoords[layout.getX(initialPair.second) to layout.getY(initialPair.second)]!!))*/
                    }
                }
            }

        }
        print("")

    }

// WIP END

    fun Graph<Number, Number>.renderGraph(pairs: Map<Pair<Number, Number>, MutableList<Pair<Number, Number>>>) {
        print("Rendering...")
        //val pairs = vertices.findAllPairs()
        //val layout = FRLayout(this)
        val images = mutableListOf<BufferedImage>()
        val layout = FRLayout(this).apply {
            size = Dimension(1024, 1024)
        }
        val persist = MyPersist(layout)
        persist.restore(file.absolutePath)
        layout.lock(false)
        VisualizationViewer(persist, persist.size).let { vv ->
            vv.run {

                background = Color.WHITE
                renderer.vertexLabelRenderer.position = Renderer.VertexLabel.Position.CNTR
                renderContext.let { rc ->


                    rc.vertexFontTransformer = Function { Font("Serif", Font.BOLD, 36) }
                    rc.setVertexLabelTransformer { vertex ->

                        layout.getX(vertex).toString().take(6)
                    }
//                multiLayerTransformer.apply {
//                    getTransformer(Layer.LAYOUT).rotate(Math.PI / 2, Point2D.Double(width / 2.0, height / 2.0))
//                }
                    rc.edgeShapeTransformer = EdgeShape.line(this@renderGraph)
                    rc.setVertexShapeTransformer {
                        Ellipse2D.Double().apply {
                            width = 60.0
                            height = 60.0
                            x -= width / 2
                            y -= height / 2
                        }

                    }

                    /*  (0..3).forEach {
                          println("Rotated 90")
                          println("OLD" + layout.getX(1))
                          rc.multiLayerTransformer.setTransformer(Layer.LAYOUT, MutableAffineTransformer().apply {
                              rotate(Math.PI / 2, Point2D.Double(layout.getX(4), layout.getY(4)))
                          })
                         // rc.multiLayerTransformer.getTransformer(Layer.LAYOUT).rotate(Math.PI / 2, Point2D.Double(layout.getX(4), layout.getY(4)))

                          println("NEW" + layout.getX(1))
                      }*/

                    val persistButton = JButton("Rotate".toUpperCase())
                    add(persistButton)

                    // persistButton.addActionListener {
                    (0..3).forEach {
                        println("Rotated 90")
                        println("OLD" + layout.getX(1))


                        // layout.setInitializer(vv.graphLayout)
                        rc.multiLayerTransformer.getTransformer(Layer.LAYOUT).rotate(Math.PI / 2, Point2D.Double(layout.getX(4), layout.getY(4)))
                        LayoutTransition(vv, vv.graphLayout, layout)


                        println("NEW" + layout.getX(1))
                    }
                    // }
                }

                //persist.restore(file.absolutePath)
                //pairs.forEach { key, combos ->
                //   combos.forEachIndexed { index, pair ->
                /*  val jLabel = JLabel().apply {
                      font = Font("Serif", Font.BOLD, 35)
                      text = when (index) {
                          0 -> "Binary Configuration: $key"
                          else -> "Pair $index: $pair"
                      }
                      isOpaque = true
                      background = Color.WHITE
                      setBounds(20, 10, 500, 50)
                  }*/
                /* renderContext.let { rc ->
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
                 }*/


                /*  val persistButton = JButton("Save Layout".toUpperCase())
                  persistButton.addActionListener {
                      print("dsjklfsd")
                      persist.persistJ(file.absolutePath)
                  }
                  add(persistButton)*/




                showJFrame(this)
//            com.montecarlo.app.saveImage(key = key, pair = pair, idx = index) //VisualizationImageServer needed here.
                //images.add(getImage(Point2D.Double(graphLayout.size.width / 2.0, graphLayout.size.width / 2.0), Dimension(720, 720)) as BufferedImage)
                //remove(jLabel)
                //}}
                //}
            }
        }
        println("Done!")


        //com.montecarlo.app.makeWordDocument(images)
    }


    fun Pair<Number, Number>.giveReflections(reflections: List<Pair<Number, Number>>): List<Pair<Number, Number>> {

        val groups = reflections.groupBy { it.first }

        val one = groups[first]!!.map { it.second }
        val two = groups[second]!!.map { it.second }

        return one.plus(two).toMutableList().findAllPairs().distinct()
    } // not needed?

    private fun showJFrame(component: JPanel) {
        JFrame("Simple Graph View").let { f ->
            f.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            f.contentPane.add(component)
            f.pack()
            f.isVisible = true
        }
    }

}