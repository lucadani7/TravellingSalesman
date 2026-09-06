package com.lucadani.model;

import lombok.Getter;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.List;
import java.util.Optional;


@Getter
public class Tour {

    private final List<City> cities;
    private final double totalDistance;

    public Tour(List<City> cities, SimpleWeightedGraph<City, DefaultWeightedEdge> graph) {
        this.cities = cities;
        this.totalDistance = calculateTotalDistance(cities, graph);
    }

    private double calculateTotalDistance(List<City> cities, SimpleWeightedGraph<City, DefaultWeightedEdge> graph) {
        if (cities == null || cities.size() < 2) {
            return 0.0;
        }
        double distance = 0.0;
        for (int i = 0; i < cities.size(); ++i) {
            City current = cities.get(i);
            City next = cities.get((i + 1) % cities.size());
            DefaultWeightedEdge edge = graph.getEdge(current, next);
            distance += Optional.ofNullable(edge).map(graph::getEdgeWeight).orElse(0.0);
        }
        return distance;
    }

}
