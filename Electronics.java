/**
 * Concrete Item subclass representing an Electronics item.
 */
public class Electronics extends Item {

    public Electronics(String id, String name, int quantity, double price) {
        super(id, name, quantity, price);
    }

    @Override
    public String getCategory() {
        return "Electronics";
    }
}
