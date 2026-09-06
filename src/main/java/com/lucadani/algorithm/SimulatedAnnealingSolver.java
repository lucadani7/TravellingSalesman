package com.lucadani.algorithm;

import com.lucadani.model.City;
import com.lucadani.model.Tour;
import lombok.AllArgsConstructor;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Metaheuristic solver that explores the space of tours by simulated annealing.
 * <p>
 * Starting from a random permutation of the cities, each iteration proposes a neighbor tour by
 * swapping two randomly chosen positions. A shorter tour is always accepted; a longer one is
 * accepted with probability {@code exp(-delta / temperature)}. The temperature starts at
 * {@code startTemperature} and is multiplied by {@code coolingRate} every iteration, so early on the
 * search wanders freely and can climb out of local optima, while later it behaves almost like plain
 * hill climbing. The shortest tour seen at any point is remembered and returned, so the result never
 * degrades even if the walk ends somewhere worse.
 * <p>
 * The search stops after {@code maxIterations} iterations or once the temperature falls below
 * {@code 1e-4}, whichever comes first. Tuning matters: a cooling rate too far below 1 freezes the
 * search before it has explored, and a start temperature too low accepts nothing uphill.
 * <p>
 * Results vary between runs, since both the initial tour and every move use an unseeded
 * {@link Random}. Instances are constructed through the Lombok-generated all-arguments constructor.
 */
@AllArgsConstructor
public class SimulatedAnnealingSolver implements TspSolver {
    /**
     * Initial temperature of the annealing schedule; higher values accept more uphill moves early on.
     */
    private final double startTemperature;

    /**
     * Multiplier applied to the temperature each iteration. Should lie in {@code (0, 1)}: values
     * closer to 1 cool slowly and explore more, at the cost of needing more iterations.
     */
    private final double coolingRate;

    /**
     * Upper bound on the number of proposed moves, capping the runtime when cooling has not yet
     * reached the freezing threshold.
     */
    private final int maxIterations;

    /**
     * {@inheritDoc}
     * <p>
     * Because the initial tour is shuffled and every move is random, repeated calls on the same graph
     * generally return different tours.
     * <p>
     * Graphs with fewer than three cities are returned in graph iteration order, as no swap can
     * change the length of such a tour.
     */
    @Override
    public Tour solve(SimpleWeightedGraph<City, DefaultWeightedEdge> graph) {
        List<City> currentTour = new ArrayList<>(graph.vertexSet());
        if (currentTour.size() < 3) {
            return new Tour(currentTour, graph);
        }
        Collections.shuffle(currentTour); // Random start
        List<City> bestTour = new ArrayList<>(currentTour);
        double currentEnergy = new Tour(currentTour, graph).getTotalDistance();
        double bestEnergy = currentEnergy;
        double temperature = startTemperature;
        Random random = new Random();
        for (int i = 0; i < maxIterations; ++i) {
            if (temperature <= 1e-4) {
                break;
            }
            List<City> newTour = new ArrayList<>(currentTour);
            int idx1 = random.nextInt(newTour.size());
            int idx2 = random.nextInt(newTour.size());
            Collections.swap(newTour, idx1, idx2);
            double newEnergy = new Tour(newTour, graph).getTotalDistance();
            if (newEnergy < currentEnergy || random.nextDouble() < Math.exp((currentEnergy - newEnergy) / temperature)) {
                currentTour = newTour;
                currentEnergy = newEnergy;

                if (currentEnergy < bestEnergy) {
                    bestEnergy = currentEnergy;
                    bestTour = new ArrayList<>(currentTour);
                }
            }
            temperature *= coolingRate;
        }
        return new Tour(bestTour, graph);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "Simulated Annealing Solver";
    }
}
