package com.lucadani.algorithm;

import com.lucadani.model.City;
import com.lucadani.model.Tour;
import lombok.AllArgsConstructor;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Local-search solver that improves an existing tour with the classic 2-opt heuristic.
 * <p>
 * The tour to improve comes from the {@link TspSolver} passed to the constructor, so this class is
 * a decorator: {@code new TwoOptSolver(new NearestNeighborSolver())} means "build a nearest
 * neighbor tour, then 2-opt it".
 * <p>
 * A 2-opt move removes two non-adjacent edges {@code (a,b)} and {@code (c,d)} from the tour and
 * reconnects the two resulting paths as {@code (a,c)} and {@code (b,d)}, which is achieved by
 * reversing the segment between {@code b} and {@code c}. The move is applied whenever it shortens
 * the tour; the scan repeats until a full pass finds no improving move, at which point the tour is
 * 2-optimal. This is a first-improvement (not best-improvement) strategy, so the outcome depends on
 * the scan order and on the initial tour, and only a local optimum is guaranteed.
 * <p>
 * One improvement pass evaluates O(n²) city pairs, each move costs O(n) for the reversal, and the
 * number of passes is not bounded a priori — this is noticeably slower than the constructive
 * solvers, but usually yields much shorter tours.
 *
 * @see LinKernighanHeuristicSolver for the more powerful 3-opt variant
 */
@AllArgsConstructor
public class TwoOptSolver implements TspSolver {
    /**
     * Solver used to produce the starting tour that this instance then improves.
     */
    private final TspSolver initialSolver;

    /**
     * Reverses in place the elements of {@code list} between the two given positions, inclusive.
     * <p>
     * This is the mutation performed by a 2-opt move: reversing the path between the two removed
     * edges is what reconnects the tour in the new order.
     *
     * @param list  the tour being modified
     * @param start index of the first element of the segment, inclusive
     * @param end   index of the last element of the segment, inclusive; if it is not greater than
     *              {@code start} the list is left unchanged
     */
    private void reverseSubsegment(List<City> list, int start, int end) {
        for (; start < end; ++start, --end) {
            City temp = list.get(start);
            list.set(start, list.get(end));
            list.set(end, temp);
        }
    }

    /**
     * Returns the travel cost between two cities, or {@code 0.0} when the graph has no such edge.
     * <p>
     * Treating a missing edge as free keeps the gain computation from throwing on incomplete graphs,
     * at the cost of making such edges look attractive to the heuristic.
     *
     * @param c1    one endpoint
     * @param c2    the other endpoint
     * @param graph the graph holding the edge weights
     * @return the weight of the edge between {@code c1} and {@code c2}, or {@code 0.0} if absent
     */
    private double getEdgeWeight(City c1, City c2, SimpleWeightedGraph<City, DefaultWeightedEdge> graph) {
        DefaultWeightedEdge edge = graph.getEdge(c1, c2);
        return Optional.ofNullable(edge).map(graph::getEdgeWeight).orElse(0.0);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Delegates to the wrapped solver for a starting tour, then repeatedly applies improving 2-opt
     * moves until a complete pass over all city pairs finds none. The pair {@code (0, n-1)} is
     * skipped because those two edges are adjacent in the cycle, so exchanging them cannot change
     * the tour length.
     * <p>
     * Tours with fewer than four cities admit no valid 2-opt move and are returned unchanged, as the
     * very object produced by the wrapped solver.
     */
    @Override
    public Tour solve(SimpleWeightedGraph<City, DefaultWeightedEdge> graph) {
        Tour initialTour = initialSolver.solve(graph);
        List<City> tour = new ArrayList<>(initialTour.getCities());
        int n = tour.size();
        if (n < 4) {
            return initialTour;
        }
        boolean improvement;
        do {
            improvement = false;
            for (int i = 0; i < n - 1; ++i) {
                for (int j = i + 2; j < n; ++j) {
                    if (i == 0 && j == n - 1) {
                        continue;
                    }
                    City a = tour.get(i);
                    City b = tour.get((i + 1) % n);
                    City c = tour.get(j);
                    City d = tour.get((j + 1) % n);
                    double originalCost = getEdgeWeight(a, b, graph) + getEdgeWeight(c, d, graph);
                    double newCost = getEdgeWeight(a, c, graph) + getEdgeWeight(b, d, graph);
                    if (newCost < originalCost) {
                        reverseSubsegment(tour, i + 1, j);
                        improvement = true;
                    }
                }
            }
        } while (improvement);
        return new Tour(tour, graph);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The returned name embeds the name of the wrapped solver, e.g.
     * {@code "2-Opt Optimization (pe Nearest Neighbor Solver)"}.
     */
    @Override
    public String getName() {
        return "2-Opt Optimization (pe " + initialSolver.getName() + ")";
    }
}
