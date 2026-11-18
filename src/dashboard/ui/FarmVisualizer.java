package dashboard.ui;

import dashboard.model.FarmComponent;
import dashboard.model.Item;
import dashboard.model.ItemContainer;

import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;


// Draws the farm on the 800 x 600 pane
public class FarmVisualizer {

    // Right side pane on the dashboard where we draw on
    private Pane pane;

    // Saves the drone so that DroneAnimator can move it
    private Group droneNode;

    // Saves the pane that the farm will be drawn on; also visualizes the pane from the dashboard
    public FarmVisualizer(Pane pane) {
        this.pane = pane;
    }

    // Clears the pane and redraws the farm, should run everytime there is a change to the farm
    public void redraw(ItemContainer root) {
        pane.getChildren().clear();
        droneNode = null;

        // Since the root has no size, we don't need to put a size and just draw what is on the inside
        for (FarmComponent child : root.getChildren()) {
        draw(child);
        }
    }

    public void draw(FarmComponent component) {
        double x = component.getLocationX();
        double y = component.getLocationY();


        Rectangle box = new Rectangle(x, y, component.getLength(), component.getWidth());

        //Makes the containers light blue and see through and items are tan
        if (component instanceof ItemContainer) {
            box.setFill(Color.LIGHTBLUE);
            box.setOpacity(0.5);
        } else {
            box.setFill(Color.BURLYWOOD);
        }
        box.setStroke(Color.BLACK);

        // The name goes inside the top left of the box
        Text label = new Text(x + 3, y + 12, component.getName());

        // Groups the box and label so that so they move together
        Group group = new Group(box, label);
        pane.getChildren().add(group);

        // this makes it so that it remembers the drone so that it can be animated later
        if (component.getName().equalsIgnoreCase("drone")) {
            droneNode = group;
        }

        // draws the children after the container so that they will show on top
        if (component instanceof ItemContainer) {
            ItemContainer container = (ItemContainer) component;
            for (FarmComponent child : container.getChildren()) {
                draw(child);
            }
        }
    }

    // returns the drone group, or returns null if there are no drones on the farm.
    public Group getDroneNode() {
        return droneNode;
    }

}

