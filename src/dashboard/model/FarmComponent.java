package dashboard.model;



import java.util.Collections;
import java.util.List;

// "abstract" means  you can never do new FarmComponent(...)
// it only exists so Item and ItemContainer can share everything below
public abstract class FarmComponent {

    // the 7 variables the assignment requires for items and containers
    private String name;
    private double price;
    private double locationX;   
    private double locationY;   
    private double length;
    private double width;
    private double height;

    // constructors
    public FarmComponent(String name, double price, double locationX, double locationY,
                         double length, double width, double height) {
        this.name = name;
        this.price = price;
        this.locationX = locationX;
        this.locationY = locationY;
        this.length = length;
        this.width = width;
        this.height = height;
    }
    
    public List<FarmComponent> getChildren() {
        return Collections.emptyList();
    }
    

    // the composite "shared operation". every subclass must define this,
    // but Item and ItemContainer will do it differently
    public abstract double getTotalPrice();

    // TreeView uses toString() as the label it displays for each node
    @Override
    public String toString() {
        return name;
    }

    // getters and setters
    // the setters are what "change name / price / location-x , etc" commands will call
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getLocationX() { return locationX; }
    public void setLocationX(double locationX) { this.locationX = locationX; }

    public double getLocationY() { return locationY; }
    public void setLocationY(double locationY) { this.locationY = locationY; }

    public double getLength() { return length; }
    public void setLength(double length) { this.length = length; }

    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
}