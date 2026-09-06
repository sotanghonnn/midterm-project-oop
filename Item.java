/**
 * Abstract base class representing a generic inventory item.
 * Demonstrates ENCAPSULATION: all fields are private and accessed only
 * through public getters/setters.
 * Demonstrates ABSTRACTION: getCategory() is abstract - each concrete
 * subclass (Clothing, Electronics, Entertainment) must define its own
 * category, and the rest of the program works only with the abstract
 * Item type without needing to know the concrete subclass.
 */
public abstract class Item {
    private String id;
    private String name;
    private int quantity;
    private double price;

    public Item(String id, String name, int quantity, double price) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Abstract method - forces every subclass to declare its own category.
     * This is the core abstraction point of the hierarchy.
     */
    public abstract String getCategory();

    /**
     * Row format used when printing this item in a table (without category).
     */
    public String toRow() {
        return String.format("%-10s %-20s %-10d %-10.2f", id, name, quantity, price);
    }

    /**
     * Row format used when printing this item in a table (with category).
     */
    public String toRowWithCategory() {
        return String.format("%-10s %-20s %-10d %-10.2f %-15s", id, name, quantity, price, getCategory());
    }
}
