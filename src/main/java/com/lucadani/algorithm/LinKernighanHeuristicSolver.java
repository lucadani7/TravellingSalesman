package com.lucadani.algorithm;

import com.lucadani.model.City;
import com.lucadani.model.Tour;
import lombok.AllArgsConstructor;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * Local-search solver in the Lin-Kernighan family, using a fixed 3-opt neighbourhood.
 * <p>
 * Like {@link TwoOptSolver}, this is a decorator: the tour to improve comes from the
 * {@link TspSolver} passed to the constructor. It then repeatedly looks for a 3-opt move — cutting
 * the tour at three positions and reconnecting the three segments in a different order — and applies
 * the first one that shortens the tour, stopping when no improving move remains.
 * <p>
 * Because the 3-opt neighbourhood strictly contains the 2-opt one, this solver escapes local optima
 * that stall 2-opt and usually produces shorter tours. The cost is steep: a single scan enumerates
 * O(n³) index triples and rebuilds and re-measures a whole candidate tour for each, so runtime grows
 * sharply with the number of cities.
 * <p>
 * Note that this is the simplified, fixed-depth variant of Lin-Kernighan: it does not perform the
 * variable-depth sequential edge exchanges of the original algorithm, despite what the display name
 * suggests. Instances are constructed through the Lombok-generated all-arguments constructor.
 */
@AllArgsConstructor
public class LinKernighanHeuristicSolver implements TspSolver {
    /**
     * Solver used to produce the starting tour that this instance then improves.
     */
    private final TspSolver initialSolver;

    /**
     * Scans the 3-opt neighbourhood of {@code tour} and returns the first candidate shorter than
     * {@code currentDist}.
     * <p>
     * Triples are enumerated with {@code i < j < k} and at least two positions apart, so that each
     * reconnection is a genuine change. The triple whose cuts fall at both ends of the list is
     * skipped, as those edges are adjacent in the cycle.
     *
     * @param tour        the current tour
     * @param graph       the graph supplying the edge weights used to measure candidates
     * @param currentDist the length of {@code tour}, the threshold a candidate must beat
     * @return a strictly shorter tour as a new list, or {@code null} if the neighbourhood holds no
     *         improvement, meaning {@code tour} is 3-optimal for this move set
     */
    private List<City> findBetterThreeOpt(List<City> tour, SimpleWeightedGraph<City, DefaultWeightedEdge> graph, double currentDist) {
        int n = tour.size();
        for (int i = 0; i < n - 2; ++i) {
            for (int j = i + 2; j < n - 1; ++j) {
                for (int k = j + 2; k < n; ++k) {
                    if (i == 0 && k == n - 1) {
                        continue;
                    }
                    List<City> candidate = applyThreeOpt(tour, i, j, k);
                    double candidateDist = new Tour(candidate, graph).getTotalDistance();
                    if (candidateDist < currentDist) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Builds the tour obtained by cutting {@code tour} after positions {@code i}, {@code j} and
     * {@code k} and reconnecting the pieces with both middle segments reversed.
     * <p>
     * The result is the concatenation of {@code [0..i]}, the segment {@code (i..j]} in reverse, the
     * segment {@code (j..k]} in reverse, and the untouched tail {@code (k..n)}. This is one of the
     * several possible 3-opt reconnections, and the only one this solver considers.
     *
     * @param tour the tour to derive the candidate from; left unmodified
     * @param i    end of the first, unchanged prefix
     * @param j    end of the first reversed segment
     * @param k    end of the second reversed segment
     * @return a new list holding the reconnected tour, of the same length as {@code tour}
     */
    private List<City> applyThreeOpt(List<City> tour, int i, int j, int k) {
        List<City> newTour = new ArrayList<>();
        int n = tour.size();
        for (int c = 0; c <= i; ++c) {
            newTour.add(tour.get(c));
        }
        for (int c = j; c > i; --c) {
            newTour.add(tour.get(c));
        }
        for (int c = k; c > j; --c) {
            newTour.add(tour.get(c));
        }
        for (int c = k + 1; c < n; ++c) {
            newTour.add(tour.get(c));
        }
        return newTour;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Delegates to the wrapped solver for a starting tour, then applies improving 3-opt moves one at
     * a time until {@link #findBetterThreeOpt} reports that none is left.
     * <p>
     * Tours with fewer than five cities are returned unchanged, as the very object produced by the
     * wrapped solver, since three well-separated cut positions do not exist below that size.
     */
    @Override
    public Tour solve(SimpleWeightedGraph<City, DefaultWeightedEdge> graph) {
        Tour initialTour = initialSolver.solve(graph);
        List<City> tour = new ArrayList<>(initialTour.getCities());
        if (tour.size() < 5) {
            return initialTour;
        }
        double currentDist = initialTour.getTotalDistance();
        boolean improvement;
        do {
            improvement = false;
            List<City> betterTour = findBetterThreeOpt(tour, graph, currentDist);
            if (betterTour != null) {
                tour = betterTour;
                currentDist = new Tour(tour, graph).getTotalDistance();
                improvement = true;
            }
        } while (improvement);
        return new Tour(tour, graph);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The returned name embeds the name of the wrapped solver, e.g.
     * {@code "Lin-Kernighan Variable 3-Opt (pe Nearest Neighbor Solver)"}.
     */
    @Override
    public String getName() {
        return "Lin-Kernighan Variable 3-Opt (pe " + initialSolver.getName() + ")";
    }
}
