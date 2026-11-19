package dashboard.ui;

import dashboard.model.FarmComponent;
import dashboard.model.ItemContainer;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

// moves the drone around the farm for visit and scan
// the drone is the item named drone and its location is home
// only the picture moves and the model stays the same
public class DroneAnimator {

    // size of the farm in pixels
    private static final double FARM_WIDTH = 800;
    private static final double FARM_HEIGHT = 600;

    // how fast the drone flies
    private static final double SPEED_PX_PER_SEC = 200;

    // gap between rows and distance from the edge during a scan
    private static final double SCAN_SPACING = 60;
    private static final double SCAN_MARGIN = 30;

    // how long the drone waits over an item
    private static final Duration HOVER_TIME = Duration.seconds(1.2);

    private final FarmVisualizer visualizer;
    private final ItemContainer root;

    // the flight that is playing now or null
    private Animation current;

    // runs after a flight ends
    private Runnable onFinished;

    // needs the visualizer for the drone picture and the root to find the drone item
    public DroneAnimator(FarmVisualizer visualizer, ItemContainer root) {
        this.visualizer = visualizer;
        this.root = root;
    }

    // lets the controller react when a flight ends
    public void setOnFinished(Runnable callback) {
        this.onFinished = callback;
    }

    // visit with a return trip by default
    public boolean visitComponent(FarmComponent target) {
        return visitComponent(target, true);
    }

    // flies to the center of the target and can fly back after
    // returns false if there is no drone or no target
    public boolean visitComponent(FarmComponent target, boolean returnHome) {
        stop();

        FarmComponent drone = findDrone(root);
        Group node = visualizer.getDroneNode();
        if (drone == null || node == null || target == null) {
            return false;
        }

        Point2D home = centerOf(drone);
        Point2D start = currentCenter(node, home);
        Point2D destination = clampToFarm(centerOf(target));

        SequentialTransition flight = new SequentialTransition();
        flight.getChildren().add(leg(node, home, start, destination));
        if (returnHome) {
            flight.getChildren().add(new PauseTransition(HOVER_TIME));
            flight.getChildren().add(leg(node, home, destination, home));
        }
        play(flight, node);
        return true;
    }

    // flies back and forth over the whole farm and then goes home
    // returns false if there is no drone
    public boolean scanFarm() {
        stop();

        FarmComponent drone = findDrone(root);
        Group node = visualizer.getDroneNode();
        if (drone == null || node == null) {
            return false;
        }

        Point2D home = centerOf(drone);

        // build the rows and swap direction after each one
        List<Point2D> waypoints = new ArrayList<>();
        double left = SCAN_MARGIN;
        double right = FARM_WIDTH - SCAN_MARGIN;
        boolean leftToRight = true;
        for (double y = SCAN_MARGIN; y <= FARM_HEIGHT - SCAN_MARGIN; y += SCAN_SPACING) {
            waypoints.add(new Point2D(leftToRight ? left : right, y));
            waypoints.add(new Point2D(leftToRight ? right : left, y));
            leftToRight = !leftToRight;
        }
        waypoints.add(home);

        // one straight leg between each pair of points
        SequentialTransition flight = new SequentialTransition();
        Point2D from = currentCenter(node, home);
        for (Point2D to : waypoints) {
            flight.getChildren().add(leg(node, home, from, to));
            from = to;
        }
        play(flight, node);
        return true;
    }

    // stops the flight so call this before every redraw
    public void stop() {
        if (current != null) {
            current.stop();
            current = null;
        }
    }

    // true while a flight is playing
    public boolean isFlying() {
        return current != null && current.getStatus() == Animation.Status.RUNNING;
    }

    // puts the drone picture back at home right away
    public void resetToHome() {
        stop();
        Group node = visualizer.getDroneNode();
        if (node != null) {
            node.setTranslateX(0);
            node.setTranslateY(0);
        }
    }

    // starts the flight and keeps the drone on top of other shapes
    private void play(Animation animation, Group node) {
        current = animation;
        node.toFront();
        animation.setOnFinished(e -> {
            current = null;
            if (onFinished != null) {
                onFinished.run();
            }
        });
        animation.play();
    }

    // one straight flight between two center points
    // the group starts at translate zero so translate is the point minus home
    private TranslateTransition leg(Group node, Point2D home, Point2D from, Point2D to) {
        double seconds = Math.max(0.3, from.distance(to) / SPEED_PX_PER_SEC);
        TranslateTransition t = new TranslateTransition(Duration.seconds(seconds), node);
        t.setFromX(from.getX() - home.getX());
        t.setFromY(from.getY() - home.getY());
        t.setToX(to.getX() - home.getX());
        t.setToY(to.getY() - home.getY());
        t.setInterpolator(Interpolator.LINEAR);
        return t;
    }

    // where the drone center is right now
    private Point2D currentCenter(Group node, Point2D home) {
        return new Point2D(home.getX() + node.getTranslateX(),
                           home.getY() + node.getTranslateY());
    }

    // center of a box as the visualizer draws it
    // length is horizontal and width is vertical
    private Point2D centerOf(FarmComponent c) {
        return new Point2D(c.getLocationX() + c.getLength() / 2.0,
                           c.getLocationY() + c.getWidth() / 2.0);
    }

    // keeps a point inside the farm
    private Point2D clampToFarm(Point2D p) {
        return new Point2D(Math.max(0, Math.min(FARM_WIDTH, p.getX())),
                           Math.max(0, Math.min(FARM_HEIGHT, p.getY())));
    }

    // searches the tree for the item named drone and ignores letter case
    private FarmComponent findDrone(FarmComponent node) {
        for (FarmComponent child : node.getChildren()) {
            if (child.getName().equalsIgnoreCase("drone")) {
                return child;
            }
            FarmComponent found = findDrone(child);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
