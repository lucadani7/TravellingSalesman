package com.lucadani.gui;

import com.lucadani.algorithm.*;
import com.lucadani.model.City;
import com.lucadani.model.Tour;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {
    private final GraphCanvas canvas;
    private final JComboBox<TspSolver> solverComboBox;
    private final JLabel statusLabel;

    public MainFrame() {
        setTitle("TSP Solver Suite - Java Swing");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        canvas = new GraphCanvas();
        statusLabel = new JLabel("Click on screen to add cities.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));

        canvas.setOnCityAddedListener(city -> statusLabel.setText("City added: " + city.name() + " (" + (int)city.x() + ", " + (int)city.y() + ")"));
        add(canvas, BorderLayout.CENTER);

        TspSolver[] solvers = getSolvers();

        solverComboBox = new JComboBox<>(solvers);
        solverComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TspSolver) {
                    setText(((TspSolver) value).getName());
                }
                return this;
            }
        });

        JButton solveButton = new JButton("Compute Tour");
        JButton clearButton = new JButton("Reset (Clear)");

        solveButton.addActionListener(e -> runSelectedSolver());
        clearButton.addActionListener(e -> {
            canvas.clear();
            statusLabel.setText("Map reset.");
        });

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        controlPanel.add(new JLabel("Algorithm:"));
        controlPanel.add(solverComboBox);
        controlPanel.add(solveButton);
        controlPanel.add(clearButton);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.add(controlPanel);
        bottomPanel.add(statusLabel);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private static TspSolver[] getSolvers() {
        TspSolver nn = new NearestNeighborSolver();
        return new TspSolver[]{
                nn,
                new TwoOptSolver(nn),
                new SimulatedAnnealingSolver(1000.0, 100.0, 1),
                new GeneticAlgorithmSolver(50, 100, 0.1),
                new AntColonyOptimizationSolver(10, 20, 0.1, 1.0, 2.0),
                new ChristofidesSolver(),
                new LinKernighanHeuristicSolver(nn)
        };
    }

    private void runSelectedSolver() {
        List<City> cities = canvas.getCities();
        if (cities.size() < 3) {
            JOptionPane.showMessageDialog(this, "Add at least 3 cities on the map!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        SimpleWeightedGraph<City, DefaultWeightedEdge> graph = createGraph(cities);
        TspSolver selectedSolver = (TspSolver) solverComboBox.getSelectedItem();
        if (selectedSolver != null) {
            long startTime = System.currentTimeMillis();
            Tour currentTour = selectedSolver.solve(graph);
            long duration = System.currentTimeMillis() - startTime;
            canvas.setCurrentTour(currentTour);
            statusLabel.setText(String.format("Algorithm: %s | Distance: %.2f | Time: %d ms", selectedSolver.getName(), currentTour.getTotalDistance(), duration));
        }
    }

    private static SimpleWeightedGraph<City, DefaultWeightedEdge> createGraph(List<City> cities) {
        SimpleWeightedGraph<City, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        for (City c : cities) {
            graph.addVertex(c);
        }
        for (int i = 0; i < cities.size(); ++i) {
            for (int j = i + 1; j < cities.size(); ++j) {
                City u = cities.get(i);
                City v = cities.get(j);
                DefaultWeightedEdge edge = graph.addEdge(u, v);
                if (edge != null) {
                    graph.setEdgeWeight(edge, u.distanceTo(v));
                }
            }
        }
        return graph;
    }
}
