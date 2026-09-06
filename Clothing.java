/**
 * Concrete Item subclass representing a Clothing item.
 */
public class Clothing extends Item {

    public Clothing(String id, String name, int quantity, double price) {
        super(id, name, quantity, price);
    }

    @Override
    public String getCategory() {
        return "Clothing";
    }
}
