package com.lucadani.model;

public record City(String name, double x, double y) {
    public double distanceTo(City other) {
        double deltaX = this.x - other.x;
        double deltaY = this.y - other.y;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
}
