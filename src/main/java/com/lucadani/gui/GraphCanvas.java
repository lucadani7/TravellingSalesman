package com.lucadani.gui;

import com.lucadani.model.City;
import com.lucadani.model.Tour;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

public class GraphCanvas extends JPanel {
    @Getter
    private final List<City> cities = new ArrayList<>();
    private Tour currentTour;
    @Setter
    private Consumer<City> onCityAddedListener;

    public GraphCanvas() {
        setBackground(Color.WHITE);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String cityName = "C" + (cities.size() + 1);
                City newCity = new City(cityName, e.getX(), e.getY());
                cities.add(newCity);
                currentTour = null;
                if (onCityAddedListener != null) {
                    onCityAddedListener.accept(newCity);
                }
                repaint();
            }
        });
    }

    public void setCurrentTour(Tour currentTour) {
        this.currentTour = currentTour;
        repaint();
    }

    public void clear() {
        cities.clear();
        currentTour = null;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (currentTour != null && currentTour.getCities() != null) {
            g2d.setColor(new Color(41, 128, 185));
            g2d.setStroke(new BasicStroke(2.5f));
            List<City> tourCities = currentTour.getCities();
            for (int i = 0; i < tourCities.size(); ++i) {
                City c1 = tourCities.get(i);
                City c2 = tourCities.get((i + 1) % tourCities.size());
                g2d.drawLine((int) c1.x(), (int) c1.y(), (int) c2.x(), (int) c2.y());
            }
        }
        for (City c : cities) {
            int radius = 8;
            g2d.setColor(new Color(231, 76, 60));
            g2d.fillOval((int) c.x() - radius, (int) c.y() - radius, radius * 2, radius * 2);
            g2d.setColor(Color.DARK_GRAY);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2d.drawString(c.name(), (int) c.x() + 10, (int) c.y() + 4);
        }
    }
}
