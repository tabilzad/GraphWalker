import edu.uci.ics.jung.graph.Hypergraph
import edu.uci.ics.jung.graph.SetHypergraph
import edu.uci.ics.jung.io.graphml.EdgeMetadata
import edu.uci.ics.jung.io.graphml.GraphMetadata
import edu.uci.ics.jung.io.graphml.HyperEdgeMetadata
import edu.uci.ics.jung.io.graphml.NodeMetadata
import com.google.common.base.Function
/**
 * Created by FERMAT on 10/30/2017.
 */
class graphFactory(): Function<GraphMetadata, Hypergraph<Number, Number>> {
    override fun apply(input: GraphMetadata?): Hypergraph<Number, Number>? {
        return SetHypergraph()
    }
}

class vertexFactory: Function<NodeMetadata, Number> {
    internal var n = 0
    override fun apply(md: NodeMetadata?): Number {
        return n++
    }
}

class edgeFactory: Function<EdgeMetadata, Number> {
    internal var n = 100

    override fun apply(md: EdgeMetadata?): Number {
        return n++
    }
}

class hyperEdgeFactory: Function<HyperEdgeMetadata, Number> {
    internal var n = 0

    override fun apply(md: HyperEdgeMetadata?): Number {
        return n++
    }
}