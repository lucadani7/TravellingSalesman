package com.lucadani.algorithm;

import com.lucadani.model.City;
import com.lucadani.model.Tour;
import org.jgrapht.alg.tour.ChristofidesThreeHalvesApproxMetricTSP;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * Solver that delegates to JGraphT's implementation of the Christofides algorithm.
 * <p>
 * Christofides builds a minimum spanning tree, adds a minimum-weight perfect matching over the
 * vertices of odd degree, and shortcuts the resulting Eulerian circuit into a Hamiltonian tour. On a
 * metric instance this guarantees a tour no longer than 3/2 of the optimum — the best approximation
 * ratio offered by any solver in this package.
 * <p>
 * That guarantee holds only when the edge weights form a metric: they must be symmetric and satisfy
 * the triangle inequality, as the Euclidean distances from {@link City#distanceTo(City)} do. Feeding
 * a non-metric or incomplete graph still returns a tour, but without any bound on its length.
 *
 * @see ChristofidesThreeHalvesApproxMetricTSP
 * @see MstApproximationSolver for the simpler, weaker 2-approximation
 */
public class ChristofidesSolver implements TspSolver {
    /**
     * {@inheritDoc}
     * <p>
     * JGraphT returns a closed walk whose first and last vertex coincide; the duplicated final city
     * is stripped here to match the open-list convention of {@link Tour}.
     * <p>
     * Graphs with fewer than three vertices are returned as-is, since Christofides needs a cycle to
     * work with.
     */
    @Override
    public Tour solve(SimpleWeightedGraph<City, DefaultWeightedEdge> graph) {
        List<City> vertices = new ArrayList<>(graph.vertexSet());
        if (vertices.size() < 3) {
            return new Tour(vertices, graph);
        }
        ChristofidesThreeHalvesApproxMetricTSP<City, DefaultWeightedEdge> christofides = new ChristofidesThreeHalvesApproxMetricTSP<>();
        List<City> tourCities = christofides.getTour(graph).getVertexList();
        if (tourCities.size() > 1 && tourCities.getFirst().equals(tourCities.getLast())) {
            tourCities.removeLast();
        }
        return new Tour(tourCities, graph);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "Christofides 3/2-Approximation (JGraphT)";
    }
}
