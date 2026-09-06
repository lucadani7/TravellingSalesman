package com.lucadani.algorithm;

import com.lucadani.model.City;
import com.lucadani.model.Tour;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm;
import org.jgrapht.alg.spanning.PrimMinimumSpanningTree;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.ArrayList;
import java.util.List;

/**
 * Constructive solver based on the double-tree (MST preorder) approximation.
 * <p>
 * The algorithm computes a minimum spanning tree of the graph with Prim's algorithm, then walks that
 * tree depth-first, and records the vertices in the order they are first reached. Since the MST is a
 * lower bound on the optimal tour and the preorder walk shortcuts a traversal that uses every tree
 * edge twice, the tour is at most twice the optimum on metric instances.
 * <p>
 * That factor-2 bound is weaker than the 3/2 of {@link ChristofidesSolver} and, as with Christofides,
 * relies on the edge weights being symmetric and satisfying the triangle inequality. In exchange the
 * algorithm is straightforward and fast: one MST plus one traversal, with no matching step.
 */
public class MstApproximationSolver implements TspSolver {
    /**
     * {@inheritDoc}
     * <p>
     * The spanning tree edges are copied into a scratch graph so that a
     * {@link DepthFirstIterator} can traverse the tree alone; the original graph is untouched and is
     * still the one used to measure the resulting tour. The traversal starts at the first vertex in
     * graph iteration order.
     * <p>
     * An empty graph yields an empty tour. On a disconnected graph, Prim's algorithm spans only the
     * component reachable from the start vertex, so the tour may omit cities.
     */
    @Override
    public Tour solve(SimpleWeightedGraph<City, DefaultWeightedEdge> graph) {
        if (graph.vertexSet().isEmpty()) {
            return new Tour(List.of(), graph);
        }
        PrimMinimumSpanningTree<City, DefaultWeightedEdge> prim = new PrimMinimumSpanningTree<>(graph);
        SpanningTreeAlgorithm.SpanningTree<DefaultWeightedEdge> mst = prim.getSpanningTree();
        SimpleWeightedGraph<City, DefaultWeightedEdge> mstGraph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        for (City vertex : graph.vertexSet()) {
            mstGraph.addVertex(vertex);
        }
        for (DefaultWeightedEdge edge : mst.getEdges()) {
            City source = graph.getEdgeSource(edge);
            City target = graph.getEdgeTarget(edge);
            DefaultWeightedEdge newEdge = mstGraph.addEdge(source, target);
            if (newEdge != null) {
                mstGraph.setEdgeWeight(newEdge, graph.getEdgeWeight(edge));
            }
        }
        List<City> tour = new ArrayList<>();
        City startVertex = graph.vertexSet().iterator().next();
        for (var dfs = new DepthFirstIterator<>(mstGraph, startVertex); dfs.hasNext(); ) {
            tour.add(dfs.next());
        }
        return new Tour(tour, graph);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "MST Preorder Approximation";
    }
}
