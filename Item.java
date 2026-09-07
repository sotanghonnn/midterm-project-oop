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

    // Fixed column widths used for table output. Keeping these here (next to
    // toRow()/toRowWithCategory()) guarantees the header printed in Main and
    // the data rows printed here always use the exact same widths, so
    // columns line up regardless of how long an ID or Name is.
    static final int ID_WIDTH = 12;
    static final int NAME_WIDTH = 21;
    static final int QUANTITY_WIDTH = 12;
    static final int PRICE_WIDTH = 13;
    static final int CATEGORY_WIDTH = 15;

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
     * If a value would be longer than its allocated column width, it is
     * truncated (with a trailing "...") instead of being allowed to spill
     * into - and misalign - the next column.
     */
    private static String fit(String value, int width) {
        if (value.length() > width - 1) {
            int cut = Math.max(0, width - 4);
            return value.substring(0, cut) + "...";
        }
        return value;
    }

    /**
     * Row format used when printing this item in a table (without category).
     * Uses fixed-width printf-style formatting so columns stay aligned no
     * matter how long the ID or Name is.
     */
    public String toRow() {
        return String.format("%-" + ID_WIDTH + "s%-" + NAME_WIDTH + "s%-" + QUANTITY_WIDTH + "d%-" + PRICE_WIDTH + ".2f",
                fit(id, ID_WIDTH), fit(name, NAME_WIDTH), quantity, price);
    }

    /**
     * Row format used when printing this item in a table (with category).
     */
    public String toRowWithCategory() {
        return String.format("%-" + ID_WIDTH + "s%-" + NAME_WIDTH + "s%-" + QUANTITY_WIDTH + "d%-" + PRICE_WIDTH + ".2f%-" + CATEGORY_WIDTH + "s",
                fit(id, ID_WIDTH), fit(name, NAME_WIDTH), quantity, price, getCategory());
    }
}