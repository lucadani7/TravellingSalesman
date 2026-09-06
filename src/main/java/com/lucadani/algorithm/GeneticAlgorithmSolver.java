package com.lucadani.algorithm;

import com.lucadani.model.City;
import com.lucadani.model.Tour;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Evolutionary solver that breeds a population of candidate tours over successive generations.
 * <p>
 * The initial population consists of {@code populationSize} random permutations of the cities. Each
 * generation produces a new population by repeatedly picking two parents through tournament
 * selection, recombining them with order crossover, and mutating the child with random swaps. The
 * best tour found so far is copied unchanged into every new generation (elitism), so quality never
 * regresses across generations. After {@code generations} rounds the best tour ever seen is returned.
 * <p>
 * A tour here is encoded directly as the permutation of cities, which is why crossover has to repair
 * the child to keep it a valid permutation rather than simply splicing two halves.
 * <p>
 * Results vary between runs, as the initial population and every genetic operator draw from an
 * unseeded {@link Random}. Runtime is dominated by tour-length evaluations, of which there are
 * roughly {@code generations * populationSize} selections' worth, each O(n).
 */
public class GeneticAlgorithmSolver implements TspSolver {
    /**
     * Number of candidate tours kept in each generation. Larger populations explore more but cost
     * proportionally more evaluations per generation.
     */
    private final int populationSize;

    /**
     * Number of generations to evolve before returning the best tour found.
     */
    private final int generations;

    /**
     * Per-position probability that a city is swapped with a random other during mutation. Keeps the
     * population diverse; too high a value degenerates the search into a random walk.
     */
    private final double mutationRate;

    /**
     * Source of randomness for population initialization, selection, crossover, and mutation. Unseeded,
     * so runs are not reproducible.
     */
    private final Random random = new Random();

    /**
     * Creates a solver with the given evolution parameters.
     *
     * @param populationSize number of tours per generation; must be at least 1
     * @param generations    number of generations to evolve
     * @param mutationRate   per-position swap probability, expected in {@code [0, 1]}
     */
    public GeneticAlgorithmSolver(int populationSize, int generations, double mutationRate) {
        this.populationSize = populationSize;
        this.generations = generations;
        this.mutationRate = mutationRate;
    }

    /**
     * Picks a parent by tournament selection: five individuals are drawn at random from the
     * population and the shortest of them wins.
     * <p>
     * Sampling a small subset rather than the whole population gives fitter individuals an advantage
     * without letting them take over immediately, which preserves diversity.
     *
     * @param population the current generation to draw candidates from; must not be empty
     * @param graph      the graph supplying the edge weights used to compare candidates
     * @return the shortest tour among those sampled; the list itself, not a copy
     */
    private List<City> tournamentSelection(List<List<City>> population, SimpleWeightedGraph<City, DefaultWeightedEdge> graph) {
        int tournamentSize = 5;
        List<City> best = null;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < tournamentSize; ++i) {
            List<City> candidate = population.get(random.nextInt(population.size()));
            double dist = new Tour(candidate, graph).getTotalDistance();
            if (dist < bestDist) {
                bestDist = dist;
                best = candidate;
            }
        }
        return best;
    }

    /**
     * Combines two parent tours into a child using order crossover (OX).
     * <p>
     * A random slice of {@code parent1} is copied into the child at the same positions, and the
     * remaining slots are filled with the cities of {@code parent2} in their original order, skipping
     * those already present. This preserves a contiguous run of visiting order from one parent and
     * the relative order of the rest from the other, while guaranteeing the child is still a valid
     * permutation with no duplicates.
     *
     * @param parent1 the parent contributing the copied slice; must be a permutation of the cities
     * @param parent2 the parent contributing the remaining cities in order; must be a permutation of
     *                the same cities as {@code parent1}
     * @return a new list holding the child tour, of the same length as the parents
     */
    private List<City> crossover(List<City> parent1, List<City> parent2) {
        List<City> child = new ArrayList<>(Collections.nCopies(parent1.size(), null));
        int start = random.nextInt(parent1.size());
        int end = random.nextInt(parent1.size());
        for (int i = Math.min(start, end); i <= Math.max(start, end); ++i) {
            child.set(i, parent1.get(i));
        }
        int p2Index = 0;
        for (int i = 0; i < child.size(); ++i) {
            if (child.get(i) == null) {
                while (child.contains(parent2.get(p2Index))) {
                    ++p2Index;
                }
                child.set(i, parent2.get(p2Index));
            }
        }
        return child;
    }

    /**
     * Mutates a tour in place by swap mutation.
     * <p>
     * Each position is visited once and, with probability {@code mutationRate}, exchanged with a
     * randomly chosen position. Swapping keeps the tour a valid permutation, so no repair is needed.
     *
     * @param tour the tour to mutate; modified in place
     */
    private void mutate(List<City> tour) {
        for (int i = 0; i < tour.size(); ++i) {
            if (random.nextDouble() < mutationRate) {
                int j = random.nextInt(tour.size());
                Collections.swap(tour, i, j);
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Because the population and every operator are randomized, repeated calls on the same graph
     * generally return different tours.
     * <p>
     * Graphs with fewer than four cities are returned in graph iteration order, as recombination has
     * no room to improve such a tour.
     */
    @Override
    public Tour solve(SimpleWeightedGraph<City, DefaultWeightedEdge> graph) {
        List<City> cities = new ArrayList<>(graph.vertexSet());
        if (cities.size() < 4) {
            return new Tour(cities, graph);
        }
        List<List<City>> population = new ArrayList<>();
        for (int i = 0; i < populationSize; ++i) {
            List<City> individual = new ArrayList<>(cities);
            Collections.shuffle(individual);
            population.add(individual);
        }
        List<City> bestEver = new ArrayList<>(population.getFirst());
        double bestDistance = new Tour(bestEver, graph).getTotalDistance();
        for (int gen = 0; gen < generations; ++gen) {
            List<List<City>> newPopulation = new ArrayList<>();
            newPopulation.add(new ArrayList<>(bestEver));
            while (newPopulation.size() < populationSize) {
                List<City> parent1 = tournamentSelection(population, graph);
                List<City> parent2 = tournamentSelection(population, graph);
                List<City> child = crossover(parent1, parent2);
                mutate(child);
                newPopulation.add(child);
                double dist = new Tour(bestEver, graph).getTotalDistance();
                if (dist < bestDistance) {
                    bestDistance = dist;
                    bestEver = new ArrayList<>(child);
                }
            }
            population = newPopulation;
        }
        return new Tour(bestEver, graph);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "Genetic Algorithm Solver";
    }
}
