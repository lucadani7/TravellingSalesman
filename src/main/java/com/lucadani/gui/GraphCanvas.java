package com.lucadani.gui;

import com.lucadani.model.City;
import com.lucadani.model.Tour;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Consumer;

public class GraphCanvas extends JPanel {
    @Getter
    private final List<City> cities = new ArrayList<>();
    private Tour currentTour;
    @Setter
    private Consumer<City> onCityAddedListener;
    private double zoom = 1.0;
    private double panX = 0.0;
    private double panY = 0.0;
    private Point dragStartPoint;
    private Tour animatedTour;
    private int animationStep = 0;
    private Timer animationTimer;

    public GraphCanvas() {
        setBackground(Color.WHITE);
        setFocusable(true);

        addMouseWheelListener(e -> {
            zoom = Math.clamp(e.getWheelRotation() < 0 ? zoom * 1.1 : zoom / 1.1, 0.1, 10.0); // https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Math.html#clamp(double,double,double)
            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    dragStartPoint = e.getPoint();
                } else if (e.getButton() == MouseEvent.BUTTON1) {
                    try {
                        AffineTransform at = new AffineTransform();
                        at.translate(panX, panY);
                        at.scale(zoom, zoom);

                        java.awt.geom.Point2D worldPoint = new java.awt.geom.Point2D.Double();
                        at.inverseTransform(e.getPoint(), worldPoint);

                        String cityName = "C" + (cities.size() + 1);
                        City newCity = new City(cityName, worldPoint.getX(), worldPoint.getY());
                        cities.add(newCity);
                        currentTour = null;

                        if (onCityAddedListener != null) {
                            onCityAddedListener.accept(newCity);
                        }
                        repaint();
                    } catch (Exception ex) {
                        System.err.println(ex.getMessage());
                    }
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStartPoint != null && SwingUtilities.isRightMouseButton(e)) {
                    double dx_move = e.getX() - dragStartPoint.x;
                    double dy_move = e.getY() - dragStartPoint.y;
                    panX += dx_move;
                    panY += dy_move;
                    dragStartPoint = e.getPoint();
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStartPoint != null && SwingUtilities.isRightMouseButton(e)) {
                    double dx = e.getX() - dragStartPoint.x;
                    double dy = e.getY() - dragStartPoint.y;
                    panX += dx;
                    panY += dy;
                    dragStartPoint = e.getPoint();
                    repaint();
                }
            }
        });

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

    public void animateTour(Tour tour) {
        if (tour == null || tour.getCities() == null || tour.getCities().isEmpty()) {
            setCurrentTour(tour);
            return;
        }
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        this.animatedTour = tour;
        this.animationStep = 0;
        int totalEdges = tour.getCities().size();
        animationTimer = new Timer(80, e -> {
            if (animationStep < totalEdges) {
                ++animationStep;
                repaint();
            } else {
                animationTimer.stop();
                currentTour = animatedTour;
                animatedTour = null;
            }
        });
        animationTimer.start();
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

    public void generateRandomCities(int count) {
        clear();
        int width = getWidth();
        int height = getHeight();
        if (width <= 50) {
            width = 800;
        }
        if (height <= 50) {
            height = 600;
        }
        int margin = 40;
        Random random = new Random();
        for (int i = 1; i <= count; ++i) {
            double x = margin + random.nextDouble() * (width - 2 * margin);
            double y = margin + random.nextDouble() * (height - 2 * margin);
            cities.add(new City("C" + i, x, y));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        AffineTransform at = new AffineTransform();
        at.translate(panX, panY);
        at.scale(zoom, zoom);
        g2d.transform(at);

        Tour tourToDraw = (animatedTour != null) ? animatedTour : currentTour;

        if (tourToDraw != null && tourToDraw.getCities() != null) {
            g2d.setColor(new Color(41, 128, 185));
            g2d.setStroke(new BasicStroke(2.5f));
            List<City> tourCities = tourToDraw.getCities();
            int limit = (animatedTour != null) ? Math.min(animationStep, tourCities.size()) : tourCities.size();
            for (int i = 0; i < limit; ++i) {
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
        g2d.dispose();
    }
}
