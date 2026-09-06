package com.lucadani.algorithm;

import com.lucadani.model.City;
import com.lucadani.model.Tour;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Swarm-intelligence solver implementing Ant System, the classic form of ant colony optimization.
 * <p>
 * Every edge carries a pheromone level, initialised to {@code 1.0}. In each iteration
 * {@code numAnts} artificial ants each build a complete tour, choosing the next city at random with
 * a probability proportional to {@code pheromone^alpha * (1/distance)^beta} — that is, biased both
 * towards edges other ants have favoured and towards short edges. Afterwards all pheromone levels
 * decay by {@code evaporationRate}, and every ant deposits {@code 1/tourLength} on the edges it
 * used, so shorter tours reinforce their edges more strongly. Over {@code maxIterations} rounds the
 * colony converges on a set of well-regarded edges; the shortest tour ever built is returned.
 * <p>
 * The exponents {@code alpha} and {@code beta} set the balance between the two forces: raising
 * {@code alpha} makes the colony follow its own trails and converge sooner, possibly prematurely,
 * while raising {@code beta} makes it behave more like the greedy {@link NearestNeighborSolver}.
 * <p>
 * Results vary between runs, since city selection draws from an unseeded {@link Random}. Runtime is
 * roughly {@code maxIterations * numAnts} tour constructions, each O(n²).
 */
public class AntColonyOptimizationSolver implements TspSolver {
    /**
     * Number of ants that build a tour in each iteration.
     */
    private final int numAnts;

    /**
     * Number of iterations of the colony, i.e. how many rounds of tour building and pheromone update
     * to run.
     */
    private final int maxIterations;

    /**
     * Fraction of pheromone lost by every edge at the end of each iteration, expected in
     * {@code [0, 1]}. Higher values forget past trails faster, keeping exploration alive.
     */
    private final double evaporationRate;

    /**
     * Exponent applied to the pheromone level when computing selection probabilities; controls how
     * strongly ants follow existing trails.
     */
    private final double alpha;

    /**
     * Exponent applied to the inverse distance when computing selection probabilities; controls how
     * strongly ants prefer short edges.
     */
    private final double beta;

    /**
     * Source of randomness for the starting city and the probabilistic city selection. Unseeded, so
     * runs are not reproducible.
     */
    private final Random random = new Random();

    /**
     * Creates a solver with the given colony parameters.
     *
     * @param numAnts         number of ants per iteration
     * @param maxIterations   number of colony iterations to run
     * @param evaporationRate fraction of pheromone lost per iteration, expected in {@code [0, 1]}
     * @param alpha           pheromone influence exponent
     * @param beta            distance influence exponent
     */
    public AntColonyOptimizationSolver(int numAnts, int maxIterations, double evaporationRate, double alpha, double beta) {
        this.numAnts = numAnts;
        this.maxIterations = maxIterations;
        this.evaporationRate = evaporationRate;
        this.alpha = alpha;
        this.beta = beta;
    }

    /**
     * Walks a single ant across the graph, building one complete tour.
     * <p>
     * The ant starts at a random city and repeatedly moves to a city chosen by
     * {@link #selectNextCityIndex}, tracking the unvisited set in a {@link BitSet}, until every city
     * has been visited.
     *
     * @param cities     the cities, indexed consistently with the {@code unvisited} bit set
     * @param graph      the graph supplying edges and their weights
     * @param pheromones current pheromone level per edge
     * @param n          number of cities, i.e. {@code cities.size()}
     * @return a new list holding the tour this ant built, containing all {@code n} cities
     */
    private List<City> constructTour(List<City> cities, SimpleWeightedGraph<City, DefaultWeightedEdge> graph, Map<DefaultWeightedEdge, Double> pheromones, int n) {
        List<City> tour = new ArrayList<>(n);
        BitSet unvisited = new BitSet(n);
        unvisited.set(0, n);
        int currentIndex = random.nextInt(n);
        unvisited.clear(currentIndex);
        tour.add(cities.get(currentIndex));
        while (unvisited.cardinality() > 0) {
            int nextIndex = selectNextCityIndex(currentIndex, unvisited, cities, graph, pheromones, n);
            unvisited.clear(nextIndex);
            tour.add(cities.get(nextIndex));
            currentIndex = nextIndex;
        }
        return tour;
    }

    /**
     * Chooses the next city for an ant standing at {@code currentIndex}, by roulette-wheel selection.
     * <p>
     * Each unvisited candidate is weighted by {@code pheromone^alpha * (1/distance)^beta}, and one is
     * drawn with probability proportional to its weight. Zero-length edges are given a nominal
     * distance of {@code 1e-3} so their inverse stays finite.
     * <p>
     * When no candidate has any weight — because the current city has no edges to the unvisited ones,
     * or all their weights underflow to zero — the lowest-indexed unvisited city is returned as a
     * fallback, which keeps the tour complete at the cost of possibly using a non-existent edge.
     *
     * @param currentIndex index of the city the ant is at
     * @param unvisited    bit set of still-unvisited city indices; must have at least one bit set
     * @param cities       the cities, indexed consistently with {@code unvisited}
     * @param graph        the graph supplying edges and their weights
     * @param pheromones   current pheromone level per edge
     * @param n            number of cities, used to size the probability buffer
     * @return the index of the chosen next city
     */
    private int selectNextCityIndex(int currentIndex, BitSet unvisited, List<City> cities, SimpleWeightedGraph<City, DefaultWeightedEdge> graph, Map<DefaultWeightedEdge, Double> pheromones, int n) {
        City currentCity = cities.get(currentIndex);
        double totalProbability = 0.0;
        double[] probabilities = new double[n];
        for (int i = unvisited.nextSetBit(0); i > -1; i = unvisited.nextSetBit(i + 1)) {
            City candidateCity = cities.get(i);
            DefaultWeightedEdge edge = graph.getEdge(currentCity, candidateCity);
            if (edge != null) {
                double tau = Math.pow(pheromones.get(edge), alpha);
                double distance = graph.getEdgeWeight(edge);
                double eta = Math.pow(1.0 / (distance != 0 ? distance : 1e-3), beta);
                double value = tau * eta;
                probabilities[i] = value;
                totalProbability += value;
            }
        }
        if (totalProbability == 0.0) {
            return unvisited.nextSetBit(0);
        }
        double r = random.nextDouble() * totalProbability;
        double cumulative = 0.0;
        for (int i = unvisited.nextSetBit(0); i > -1; i = unvisited.nextSetBit(i + 1)) {
            cumulative += probabilities[i];
            if (cumulative >= r) {
                return i;
            }
        }
        return unvisited.nextSetBit(0);
    }


    /**
     * {@inheritDoc}
     * <p>
     * Runs {@code maxIterations} rounds in which every ant builds a tour, then evaporates all
     * pheromone levels and lets each ant deposit {@code 1/tourLength} on the edges of its own tour.
     * The best tour across all iterations is returned.
     * <p>
     * Because the ants' choices are randomised, repeated calls on the same graph generally return
     * different tours.
     * <p>
     * Graphs with fewer than four cities are returned in graph iteration order, as no tour choice
     * exists to be optimised.
     */
    @Override
    public Tour solve(SimpleWeightedGraph<City, DefaultWeightedEdge> graph) {
        List<City> citiesList = new ArrayList<>(graph.vertexSet());
        int n = citiesList.size();
        if (n < 4) {
            return new Tour(citiesList, graph);
        }
        Map<DefaultWeightedEdge, Double> pheromones = graph.edgeSet().stream().collect(Collectors.toMap(edge -> edge, edge -> 1.0));
        List<City> bestEverTour = null;
        double bestEverLength = Double.MAX_VALUE;
        for (int iter = 0; iter < maxIterations; ++iter) {
            List<List<City>> antTours = new ArrayList<>();
            List<Double> antTourLengths = new ArrayList<>();
            for (int ant = 0; ant < numAnts; ++ant) {
                List<City> tour = constructTour(citiesList, graph, pheromones, n);
                double length = new Tour(tour, graph).getTotalDistance();
                antTours.add(tour);
                antTourLengths.add(length);
                if (length < bestEverLength) {
                    bestEverLength = length;
                    bestEverTour = new ArrayList<>(tour);
                }
            }
            pheromones.replaceAll((edge, value) -> value * (1.0 - evaporationRate));
            for (int i = 0; i < numAnts; ++i) {
                List<City> tour = antTours.get(i);
                double length = antTourLengths.get(i);
                double deposit = 1.0 / length;
                for (int j = 0; j < tour.size(); ++j) {
                    City c1 = tour.get(j);
                    City c2 = tour.get((j + 1) % tour.size());
                    DefaultWeightedEdge edge = graph.getEdge(c1, c2);
                    if (edge != null) {
                        pheromones.merge(edge, deposit, Double::sum);
                    }
                }
            }
        }
        return new Tour(bestEverTour, graph);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "Ant Colony Optimization (ACO)";
    }
}
