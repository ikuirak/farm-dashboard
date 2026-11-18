package dashboard.model;

public class Item extends FarmComponent {

    public Item(String name, double price, double locationX, double locationY,
                double length, double width, double height) {
        super(name, price, locationX, locationY, length, width, height); // pass up to FarmComponent
    }

    // a single item's total is just its own price
    @Override
    public double getTotalPrice() {
        return getPrice();
    }
}
