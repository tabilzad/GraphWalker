package services

import com.google.common.base.Function
import domain.Lattice
import edu.uci.ics.jung.algorithms.layout.KKLayout
import edu.uci.ics.jung.graph.Graph
import edu.uci.ics.jung.visualization.VisualizationImageServer
import edu.uci.ics.jung.visualization.decorators.EdgeShape
import edu.uci.ics.jung.visualization.renderers.Renderer
import graphs
import makeWordDocument
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
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
        val graph = graphs[lattice]!!

        val combos = graph.vertices.findAllPairs().map { pair ->
            pair to mutableListOf<Pair<Number, Number>>().run {
                add(pair)
                val neighborsA = graph.getNeighbors(pair.first).toMutableList()
                neighborsA.addVirtualSites(pair.first).forEach { a ->
                    val neighborsB = graph.getNeighbors(pair.second).toMutableList()
                    neighborsB.addVirtualSites(pair.second).forEach { b ->
                        add(a to b)
                    }
                }
                this
            }
        }.associateBy({ it.first }, { it.second })

        graph.renderGraph(combos)
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