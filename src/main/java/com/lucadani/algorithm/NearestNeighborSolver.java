package com.lucadani.algorithm;

import com.lucadani.model.City;
import com.lucadani.model.Tour;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Constructive solver implementing the greedy nearest neighbour heuristic.
 * <p>
 * Starting from the first vertex in graph iteration order, the tour is grown by repeatedly hopping
 * to the closest not-yet-visited city, until every city has been added. The result is a valid tour
 * built in O(n²) time with no backtracking, which makes this the cheapest solver in the package.
 * <p>
 * The price of that speed is quality: the greedy choice tends to leave a few very long edges to be
 * closed at the end, and the tour can be arbitrarily worse than optimal. It is therefore most useful
 * as the starting point for an improvement solver such as {@link TwoOptSolver} or
 * {@link LinKernighanHeuristicSolver}.
 * <p>
 * The solver is deterministic for a given graph iteration order and holds no state, though it is not
 * declared thread-safe.
 */
public class NearestNeighborSolver implements TspSolver {
    /**
     * {@inheritDoc}
     * <p>
     * An empty graph yields an empty tour. If the current city has no edge to any unvisited city,
     * the tour is closed early and the remaining cities are left out — so on an incomplete graph the
     * result may cover only part of the vertex set.
     */
    @Override
    public Tour solve(SimpleWeightedGraph<City, DefaultWeightedEdge> graph) {
        List<City> vertices = new ArrayList<>(graph.vertexSet());
        if (vertices.isEmpty()) {
            return new Tour(List.of(), graph);
        }
        int n = vertices.size();
        BitSet visited = new BitSet(n);
        List<City> tourCities = new ArrayList<>(n);
        int currentIndex = 0;
        visited.set(currentIndex);
        tourCities.add(vertices.get(currentIndex));
        while (tourCities.size() < n) {
            int nearestIndex = -1;
            double shortestDistance = Double.MAX_VALUE;
            City currentCity = vertices.get(currentIndex);
            for (int i = 0; i < n; ++i) {
                if (!visited.get(i)) {
                    City candidateCity = vertices.get(i);
                    DefaultWeightedEdge edge = graph.getEdge(currentCity, candidateCity);
                    if (edge != null) {
                        double distance = graph.getEdgeWeight(edge);
                        if (distance < shortestDistance) {
                            shortestDistance = distance;
                            nearestIndex = i;
                        }
                    }
                }
            }
            if (nearestIndex == -1) {
                break;
            }
            visited.set(nearestIndex);
            tourCities.add(vertices.get(nearestIndex));
            currentIndex = nearestIndex;
        }
        return new Tour(tourCities, graph);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "Nearest Neighbor Solver";
    }
}
