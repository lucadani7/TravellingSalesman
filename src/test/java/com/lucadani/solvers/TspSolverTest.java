package com.lucadani.solvers;

import com.lucadani.algorithm.*;
import com.lucadani.model.City;
import com.lucadani.model.Tour;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TspSolversTest {

    private SimpleWeightedGraph<City, DefaultWeightedEdge> graph;

    @BeforeEach
    void setUp() {
        graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        City c1 = new City("A", 0.0, 0.0);
        City c2 = new City("B", 0.0, 4.0);
        City c3 = new City("C", 3.0, 4.0);
        City c4 = new City("D", 3.0, 0.0);

        graph.addVertex(c1);
        graph.addVertex(c2);
        graph.addVertex(c3);
        graph.addVertex(c4);

        // Conectăm graful complet folosind distanțele euclidiene
        for (City u : graph.vertexSet()) {
            for (City v : graph.vertexSet()) {
                if (!u.equals(v)) {
                    DefaultWeightedEdge edge = graph.addEdge(u, v);
                    if (edge != null) {
                        graph.setEdgeWeight(edge, u.distanceTo(v));
                    }
                }
            }
        }
    }

    @Test
    void testNearestNeighbor() {
        TspSolver solver = new NearestNeighborSolver();
        Tour tour = solver.solve(graph);

        assertNotNull(tour);
        assertEquals(4, tour.getCities().size());
        assertTrue(tour.getTotalDistance() > 0);
    }

    @Test
    void testTwoOptOptimization() {
        TspSolver initial = new NearestNeighborSolver();
        TspSolver solver = new TwoOptSolver(initial);
        Tour tour = solver.solve(graph);

        assertNotNull(tour);
        assertEquals(4, tour.getCities().size());
    }

    @Test
    void testSimulatedAnnealing() {
        TspSolver solver = new SimulatedAnnealingSolver(1000.0, 100.0, 1);
        Tour tour = solver.solve(graph);

        assertNotNull(tour);
        assertEquals(4, tour.getCities().size());
        assertTrue(tour.getTotalDistance() > 0);
    }

    @Test
    void testGeneticAlgorithm() {
        TspSolver solver = new GeneticAlgorithmSolver(50, 100, 0.1);
        Tour tour = solver.solve(graph);

        assertNotNull(tour);
        assertEquals(4, tour.getCities().size());
        assertTrue(tour.getTotalDistance() > 0);
    }

    @Test
    void testAntColonyOptimization() {
        TspSolver solver = new AntColonyOptimizationSolver(10, 20, 0.1, 1.0, 2.0);
        Tour tour = solver.solve(graph);

        assertNotNull(tour);
        assertEquals(4, tour.getCities().size());
        assertTrue(tour.getTotalDistance() > 0);
    }

    @Test
    void testChristofides() {
        TspSolver solver = new ChristofidesSolver();
        Tour tour = solver.solve(graph);

        assertNotNull(tour);
        assertEquals(4, tour.getCities().size());
    }

    @Test
    void testMstPreorderApprox() {
        TspSolver solver = new MstApproximationSolver();
        Tour tour = solver.solve(graph);

        assertNotNull(tour);
        assertEquals(4, tour.getCities().size());
        assertTrue(tour.getTotalDistance() > 0);
    }

    @Test
    void testLinKernighan() {
        TspSolver initial = new NearestNeighborSolver();
        TspSolver solver = new LinKernighanHeuristicSolver(initial);
        Tour tour = solver.solve(graph);

        assertNotNull(tour);
        assertEquals(4, tour.getCities().size());
    }
}