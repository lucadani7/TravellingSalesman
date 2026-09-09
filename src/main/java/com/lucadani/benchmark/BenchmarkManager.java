package com.lucadani.benchmark;

import com.lucadani.algorithm.TspSolver;
import com.lucadani.model.City;
import com.lucadani.model.Tour;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class BenchmarkManager {
    public static void runBenchmarkTask(JFrame parentFrame, TspSolver[] solvers, SimpleWeightedGraph<City, DefaultWeightedEdge> graph, BiConsumer<Tour, String> onTourSelected) {
        JDialog progressDialog = new JDialog(parentFrame, "Running Benchmark...", true);
        progressDialog.setSize(350, 130);
        progressDialog.setLocationRelativeTo(parentFrame);
        progressDialog.setLayout(new BorderLayout(10, 10));

        JProgressBar progressBar = new JProgressBar(0, solvers.length);
        progressBar.setStringPainted(true);
        JLabel progressLabel = new JLabel("Running solvers...", JLabel.CENTER);
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(progressLabel, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);
        progressDialog.add(panel, BorderLayout.CENTER);

        List<Tour> computedTours = new ArrayList<>();
        List<String> columnNames = List.of("Algorithm", "Total distance (px)", "Time (ms)");
        DefaultTableModel tableModel = new DefaultTableModel(columnNames.toArray(new Object[0]), 0);
        DefaultCategoryDataset distanceDataset = new DefaultCategoryDataset();
        DefaultCategoryDataset timeDataset = new DefaultCategoryDataset();

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                int idx = 0;
                for (TspSolver solver : solvers) {
                    try {
                        long start = System.currentTimeMillis();
                        Tour tour = solver.solve(graph);
                        long duration = System.currentTimeMillis() - start;
                        computedTours.add(tour);
                        Optional<Double> distanceOpt = Optional.ofNullable(tour)
                                .filter(t -> t.getCities() != null && !t.getCities().isEmpty())
                                .map(Tour::getTotalDistance);
                        String distanceStr = distanceOpt
                                .map(d -> String.format("%.2f", d))
                                .orElse("N/A");
                        tableModel.addRow(new Object[]{solver.getName(), distanceStr, duration});
                        String shortName = solver.getName().replaceAll("(?<!^)(?=[A-Z])", " ");
                        distanceOpt.ifPresent(d -> distanceDataset.addValue(d, "Total distance (px)", shortName));
                        timeDataset.addValue(duration, "Time (ms)", shortName);
                    } catch (Exception e) {
                        computedTours.add(null);
                        SwingUtilities.invokeLater(() -> tableModel.addRow(new Object[]{solver.getName(), "Error", -1}));
                    }
                    setProgress(idx + 1);
                }
                return null;
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                showDashboard(parentFrame, tableModel, computedTours, solvers, distanceDataset, timeDataset, onTourSelected);
            }
        };

        worker.addPropertyChangeListener(e -> {
            if ("progress".equals(e.getPropertyName())) {
                int p = (Integer) e.getNewValue();
                progressBar.setValue(p);
                progressBar.setString(p + " / " + solvers.length + " completed");
            }
        });

        worker.execute();
        progressDialog.setVisible(true);
    }



    public static void showDashboard(JFrame parentFrame, DefaultTableModel tableModel, List<Tour> computedTours, TspSolver[] solvers,
                                     DefaultCategoryDataset distanceDataset, DefaultCategoryDataset timeDataset,
                                     BiConsumer<Tour, String> onTourSelected) {

        JDialog dialog = new JDialog(parentFrame, "Benchmark Results (All the Solvers)", true);
        dialog.setSize(850, 550);
        dialog.setLocationRelativeTo(parentFrame);

        JTable table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow != -1 && selectedRow < computedTours.size()) {
                        Tour selectedTour = computedTours.get(selectedRow);
                        TspSolver selectedSolver = solvers[selectedRow];
                        if (selectedTour != null && selectedTour.getCities() != null && !selectedTour.getCities().isEmpty()) {
                            onTourSelected.accept(selectedTour, selectedSolver.getName());
                            dialog.dispose();
                        }
                    }
                }
            }
        });

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Results Table", new JScrollPane(table));
        tabs.addTab("Distances Graphics", new ChartPanel(ChartFactory.createBarChart("Comparison Distances", "Algorithm", "px", distanceDataset, PlotOrientation.VERTICAL, false, true, false)));
        tabs.addTab("Time Graphics", new ChartPanel(ChartFactory.createBarChart("Comparison Time", "Algorithm", "ms", timeDataset, PlotOrientation.VERTICAL, false, true, false)));

        dialog.add(tabs, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel bottom = new JPanel();
        bottom.add(closeBtn);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
