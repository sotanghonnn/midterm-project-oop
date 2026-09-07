import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Manages the collection of inventory Items.
 * Demonstrates ENCAPSULATION: the internal list of items is private and can
 * only be modified/read through this class's public methods.
 */
public class InventoryManager {

    private static final List<String> CATEGORIES = Arrays.asList("Clothing", "Electronics", "Entertainment");

    private List<Item> items;

    public InventoryManager() {
        this.items = new ArrayList<>();
    }

    /** Normalizes a category string to "Title Case" form e.g. "electronics" -> "Electronics". */
    public static String normalizeCategory(String category) {
        if (category == null || category.isEmpty()) {
            return category;
        }
        String trimmed = category.trim();
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }

    public boolean isValidCategory(String category) {
        return CATEGORIES.contains(normalizeCategory(category));
    }

    public boolean idExists(String id) {
        for (Item item : items) {
            if (item.getId().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    public Item findById(String id) {
        for (Item item : items) {
            if (item.getId().equalsIgnoreCase(id)) {
                return item;
            }
        }
        return null;
    }

    /** Creates the correct Item subclass based on category and adds it to the inventory. */
    public void addItem(String category, String id, String name, int quantity, double price) {
        String normalized = normalizeCategory(category);
        Item item;
        switch (normalized) {
            case "Clothing":
                item = new Clothing(id, name, quantity, price);
                break;
            case "Electronics":
                item = new Electronics(id, name, quantity, price);
                break;
            case "Entertainment":
                item = new Entertainment(id, name, quantity, price);
                break;
            default:
                throw new IllegalArgumentException("Invalid category: " + category);
        }
        items.add(item);
    }

    public boolean removeItem(String id) {
        Item item = findById(id);
        if (item == null) {
            return false;
        }
        items.remove(item);
        return true;
    }

    public List<Item> getItemsByCategory(String category) {
        String normalized = normalizeCategory(category);
        List<Item> result = new ArrayList<>();
        for (Item item : items) {
            if (item.getCategory().equalsIgnoreCase(normalized)) {
                result.add(item);
            }
        }
        return result;
    }

    public List<Item> getAllItems() {
        return items;
    }

    public List<Item> sortItems(String sortBy, String order) {
        List<Item> sorted = new ArrayList<>(items);
        Comparator<Item> comparator;

        if (sortBy.equalsIgnoreCase("quantity")) {
            comparator = Comparator.comparingInt(Item::getQuantity);
        } else {
            comparator = Comparator.comparingDouble(Item::getPrice);
        }

        if (order.equalsIgnoreCase("descending")) {
            comparator = comparator.reversed();
        }

        sorted.sort(comparator);
        return sorted;
    }

    public List<Item> getLowStockItems() {
        List<Item> result = new ArrayList<>();
        for (Item item : items) {
            if (item.getQuantity() <= 5) {
                result.add(item);
            }
        }
        return result;
    }
}