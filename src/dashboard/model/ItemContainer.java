package dashboard.model;

import java.util.ArrayList;
import java.util.List;

public class ItemContainer extends FarmComponent {

    // the "collection of items" holds Items and other ItemContainers
    private List<FarmComponent> children = new ArrayList<>();

    public ItemContainer(String name, double price, double locationX, double locationY,
                         double length, double width, double height) {
        super(name, price, locationX, locationY, length, width, height);
    }
    
    
    @Override
    public List<FarmComponent> getChildren()    { return children; }
    
    public void add(FarmComponent component)    { children.add(component); }    // add item / add item-container
    public void remove(FarmComponent component) { children.remove(component); } // delete


    // the composite pattern: own price + every child's total
    // doesnt matter whether a child is an Item or another container —
    // each child handles its own getTotalPrice(), so nesting works at any depth
    @Override
    public double getTotalPrice() {
        double total = getPrice();
        for (FarmComponent child : children) {
            total += child.getTotalPrice();
        }
        return total;
    }
}
