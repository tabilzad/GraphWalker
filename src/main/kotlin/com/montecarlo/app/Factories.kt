package com.montecarlo.app

import edu.uci.ics.jung.io.graphml.EdgeMetadata
import edu.uci.ics.jung.io.graphml.GraphMetadata
import edu.uci.ics.jung.io.graphml.HyperEdgeMetadata
import edu.uci.ics.jung.io.graphml.NodeMetadata
import com.google.common.base.Function
import edu.uci.ics.jung.graph.*

class graphFactory : Function<GraphMetadata, Graph<Number, Number>> {
    override fun apply(input: GraphMetadata?): Graph<Number, Number>? {
        return SparseGraph()
    }
}

class vertexFactory : Function<NodeMetadata, Number> {
    private var n = 0
    override fun apply(md: NodeMetadata?): Number {
        return n++
    }
}

class edgeFactory : Function<EdgeMetadata, Number> {
    private var n = 100

    override fun apply(md: EdgeMetadata?): Number {
        return n++
    }
}

class hyperEdgeFactory : Function<HyperEdgeMetadata, Number> {
    private var n = 0
    override fun apply(md: HyperEdgeMetadata?): Number {
        return n++
    }
}


