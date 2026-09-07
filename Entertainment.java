/**
 * Concrete Item subclass representing an Entertainment item.
 */
public class Entertainment extends Item {

    public Entertainment(String id, String name, int quantity, double price) {
        super(id, name, quantity, price);
    }

    @Override
    public String getCategory() {
        return "Entertainment";
    }
}