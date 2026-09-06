package com.lucadani.algorithm;

import com.lucadani.model.City;
import com.lucadani.model.Tour;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

/**
 * Common contract for every Travelling Salesman Problem strategy in this package.
 * <p>
 * A solver receives a weighted graph whose vertices are the cities to visit and whose edge weights
 * are the travel costs between them, and returns a {@link Tour} describing the visiting order it
 * found. Implementations are free to be exact, approximate, or heuristic; none of them is required
 * to return an optimal tour.
 * <p>
 * Implementations fall into two groups:
 * <ul>
 *   <li><em>constructive</em> solvers, such as {@link NearestNeighborSolver},
 *       {@link ChristofidesSolver} or {@link MstApproximationSolver}, which build a tour directly
 *       from the graph;</li>
 *   <li><em>Improvement</em> solvers, such as {@link TwoOptSolver} or
 *       {@link LinKernighanHeuristicSolver}, which delegate to another {@code TspSolver} for a
 *       starting tour and then refine it. Because they compose, an improvement solver can wrap
 *       another improvement solver.</li>
 * </ul>
 * Implementations are not required to be thread-safe.
 */
public interface TspSolver {
    /**
     * Computes a tour that visits every vertex of the given graph.
     * <p>
     * The returned tour is a closed cycle: the last city is implicitly connected back to the first
     * one, so the first city must <strong>not</strong> be repeated at the end of the list. Missing
     * edges are treated as having zero cost by {@link Tour}, so callers that need meaningful tour
     * lengths should pass a complete graph.
     * <p>
     * Degenerate inputs (empty graphs, or graphs too small for the strategy to have any freedom of
     * choice) are returned as a tour over the vertices in graph iteration order rather than
     * rejected.
     *
     * @param graph the weighted graph to solve; its vertices are the cities and its edge weights
     *              the travel costs. Must not be {@code null}, and is never modified by the solver
     * @return the tour found, together with its total length
     */
    Tour solve(SimpleWeightedGraph<City, DefaultWeightedEdge> graph);

    /**
     * Returns a human-readable name for this solver, suitable for logs and result tables.
     * <p>
     * Improvement solvers include the name of the solver they wrap, so the whole strategy chain is
     * visible in the returned string.
     *
     * @return the display name of this solver, never {@code null}
     */
    String getName();
}
