import java.util.List;
import java.util.Scanner;


public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final InventoryManager manager = new InventoryManager();

    // A plain positive whole number: no sign, no leading zero, digits only.
    // Rejects letters, decimals, scientific notation (e/E), spaces, symbols,
    // commas, and leading zeros (e.g. "0009") all in one check.
    private static final String INTEGER_FORMAT = "0|[1-9][0-9]*";

    // A positive number, whole or with a decimal part: "0", "0.5", "10",
    // "10.50" are all allowed, but "01.50", "0010", "1e2", "1.2.3" are not.
    private static final String PRICE_FORMAT = "0(\\.[0-9]+)?|[1-9][0-9]*(\\.[0-9]+)?";

    // Item ID: at least one letter, at least one digit, letters/digits only.
    // Leading zeros ARE allowed here (IDs are strings, not numeric fields).
    private static final String ID_FORMAT = "^(?=.{2,20}$)(?=.*[A-Za-z])(?=.*[0-9])[A-Za-z0-9]+$";

    // Item Name: letters, numbers, and spaces only.
    private static final String NAME_FORMAT = "^[A-Za-z0-9 ]+$";

    private static final String OVERFLOW_MESSAGE =
            "Invalid number. Please enter a valid value within the allowed range.";

    private static final int MAX_QUANTITY = 10_000;
    private static final double MAX_PRICE = 1_000_000;

    /**
     * Reads one line of input safely. If the input stream has no more lines
     * (EOF - e.g. piped input ran out, or the user pressed Ctrl+D), the
     * program exits cleanly here instead of letting Scanner throw a
     * NoSuchElementException and crash with a stack trace.
     */
    private static String safeReadLine() {
        if (!scanner.hasNextLine()) {
            System.out.println();
            System.out.println("No more input received. Exiting program. Goodbye!");
            System.exit(0);
        }
        return scanner.nextLine();
    }

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readMainMenuChoice();
            switch (choice) {
                case 1: addItem(); break;
                case 2: updateItem(); break;
                case 3: removeItem(); break;
                case 4: displayItemsByCategory(); break;
                case 5: displayAllItems(); break;
                case 6: searchItem(); break;
                case 7: sortItems(); break;
                case 8: displayLowStockItems(); break;
                case 9:
                    running = false;
                    System.out.println("Exiting program. Goodbye!");
                    break;
                default:
                    // unreachable, readMainMenuChoice already validates range
                    break;
            }
        }
        scanner.close();
    }

    // ----------------------------------------------------------------
    // Menu
    // ----------------------------------------------------------------

    private static void printMenu() {
        System.out.println();
        System.out.println("\n===== INVENTORY MANAGEMENT SYSTEM =====");
        System.out.println("1 - Add Item");
        System.out.println("2 - Update Item");
        System.out.println("3 - Remove Item");
        System.out.println("4 - Display Items by Category");
        System.out.println("5 - Display All Items");
        System.out.println("6 - Search Item");
        System.out.println("7 - Sort Items");
        System.out.println("8 - Display Low Stock Items");
        System.out.println("9 - Exit");

    }

    /**
     * Reads the main menu choice. This is the ONLY place in the program that
     * still uses numbered choices. On any invalid input (letters, decimals,
     * signs, leading zeros, scientific notation, out-of-range numbers, or an
     * overflow-sized number) it prints "Invalid menu option." and redisplays
     * the menu before asking again - it never lets an invalid value fall
     * through to the switch in main().
     */
    private static int readMainMenuChoice() {
        while (true) {
            System.out.print("Choose an option: ");
            String line = safeReadLine().trim();

            if (line.matches(INTEGER_FORMAT)) {
                try {
                    int value = Integer.parseInt(line);
                    if (value >= 1 && value <= 9) {
                        return value;
                    }
                } catch (NumberFormatException e) {
                    System.out.println(OVERFLOW_MESSAGE);
                    printMenu();
                    continue;
                }
            }
            System.out.println("Invalid menu option.");
            printMenu();
        }
    }

    // ----------------------------------------------------------------
    // Feature: Add Item
    // ----------------------------------------------------------------

    private static void addItem() {
        String category = readCategory("Enter Category (Clothing / Electronics / Entertainment): ");
        String id = readUniqueId("Enter ID: ");
        String name = readItemName("Enter Name: ");
        int quantity = readQuantity("Enter Quantity: ");
        double price = readPrice("Enter Price: ");

        manager.addItem(category, id, name, quantity, price);
        System.out.println("Item added successfully!");
    }

    // ----------------------------------------------------------------
    // Feature: Update Item
    // ----------------------------------------------------------------

    private static void updateItem() {
        if (isInventoryEmpty()) {
            return;
        }

        String id = readNonEmptyLine("Enter ID: ");
        Item item = manager.findById(id);
        if (item == null) {
            System.out.println("Item not found!");
            return;
        }

        System.out.println("Update: quantity or price");
        String fieldChoice = readWordChoice("Enter choice: ", "quantity", "price",
                "Invalid update option. Please enter quantity or price.");

        // The old value is only ever read here, BEFORE the new value is validated.
        // item.setQuantity()/setPrice() is only called once readQuantity()/readPrice()
        // has already returned a fully valid value, so an invalid entry can never
        // corrupt the existing data (Data Integrity requirement).
        if (fieldChoice.equals("quantity")) {
            int oldValue = item.getQuantity();
            int newValue = readQuantity("Enter new Quantity: ");
            item.setQuantity(newValue);
            System.out.println("Quantity of Item " + item.getName() + " is updated from " + oldValue + " to " + newValue);
        } else {
            double oldValue = item.getPrice();
            double newValue = readPrice("Enter new Price: ");
            item.setPrice(newValue);
            System.out.println("Price of Item " + item.getName() + " is updated from " + oldValue + " to " + newValue);
        }
    }

    // ----------------------------------------------------------------
    // Feature: Remove Item
    // ----------------------------------------------------------------

    private static void removeItem() {
        if (isInventoryEmpty()) {
            return;
        }

        String id = readNonEmptyLine("Enter ID: ");
        Item item = manager.findById(id);
        if (item == null) {
            System.out.println("Item not found!");
            return;
        }
        String name = item.getName();
        manager.removeItem(id);
        System.out.println("Item " + name + " has been removed from the inventory");
    }

    // ----------------------------------------------------------------
    // Feature: Display Items by Category
    // ----------------------------------------------------------------

    private static void displayItemsByCategory() {
        if (isInventoryEmpty()) {
            return;
        }

        String category = readCategory("Enter Category: ");
        List<Item> results = manager.getItemsByCategory(category);
        printTable(results, false, "No items found in category " + category + ".");
    }

    // ----------------------------------------------------------------
    // Feature: Display All Items
    // ----------------------------------------------------------------

    private static void displayAllItems() {
        if (isInventoryEmpty()) {
            return;
        }
        printTable(manager.getAllItems(), true, "No items in the inventory.");
    }

    // ----------------------------------------------------------------
    // Feature: Search Item
    // ----------------------------------------------------------------

    private static void searchItem() {
        if (isInventoryEmpty()) {
            return;
        }

        String id = readNonEmptyLine("Enter ID: ");
        Item item = manager.findById(id);
        if (item == null) {
            System.out.println("Item not found!");
            return;
        }
        System.out.println("ID: " + item.getId());
        System.out.println("Name: " + item.getName());
        System.out.println("Quantity: " + item.getQuantity());
        System.out.println("Price: " + item.getPrice());
        System.out.println("Category: " + item.getCategory());
    }

    // ----------------------------------------------------------------
    // Feature: Sort Items
    // ----------------------------------------------------------------

    private static void sortItems() {
        if (isInventoryEmpty()) {
            return;
        }

        List<Item> allItems = manager.getAllItems();

        // With only one item there is nothing meaningful to sort - just show
        // it directly instead of asking for sort-by/sort-order.
        if (allItems.size() == 1) {
            printTable(allItems, true, "No items in the inventory.");
            return;
        }

        System.out.println("Sort by: quantity or price");
        String sortBy = readWordChoice("Enter choice: ", "quantity", "price",
                "Invalid sort option. Please enter quantity or price.");

        System.out.println("Sort order: ascending or descending");
        String order = readWordChoice("Enter choice: ", "ascending", "descending",
                "Invalid sort order. Please enter ascending or descending.");

        List<Item> sorted = manager.sortItems(sortBy, order);
        printTable(sorted, true, "No items in the inventory.");
    }

    // ----------------------------------------------------------------
    // Feature: Display Low Stock Items
    // ----------------------------------------------------------------

    private static void displayLowStockItems() {
        if (isInventoryEmpty()) {
            return;
        }
        printTable(manager.getLowStockItems(), true, "No low stock items found.");
    }

    // ----------------------------------------------------------------
    // Shared helpers
    // ----------------------------------------------------------------

    /**
     * Section 19 (Empty Inventory Validation): checked before Search, Update,
     * Remove, Sort, and Display-by-Category so we never ask for an ID or a
     * category when there is nothing in the inventory to act on.
     */
    private static boolean isInventoryEmpty() {
        if (manager.getAllItems().isEmpty()) {
            System.out.println("No items in the inventory.");
            return true;
        }
        return false;
    }

    /**
     * Prints a table using fixed-width columns (matching Item.toRow() /
     * toRowWithCategory()) plus a separator line under the header, so ID,
     * Name, Quantity, Price, and Category always start at the same position
     * no matter how long any individual value is.
     */
    private static void printTable(List<Item> items, boolean withCategory, String emptyMessage) {
        if (items.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }

        int totalWidth = Item.ID_WIDTH + Item.NAME_WIDTH + Item.QUANTITY_WIDTH + Item.PRICE_WIDTH
                + (withCategory ? Item.CATEGORY_WIDTH : 0);

        if (withCategory) {
            System.out.printf("%-" + Item.ID_WIDTH + "s%-" + Item.NAME_WIDTH + "s%-" + Item.QUANTITY_WIDTH
                            + "s%-" + Item.PRICE_WIDTH + "s%-" + Item.CATEGORY_WIDTH + "s%n",
                    "ID", "Name", "Quantity", "Price", "Category");
        } else {
            System.out.printf("%-" + Item.ID_WIDTH + "s%-" + Item.NAME_WIDTH + "s%-" + Item.QUANTITY_WIDTH
                            + "s%-" + Item.PRICE_WIDTH + "s%n",
                    "ID", "Name", "Quantity", "Price");
        }
        System.out.println("-".repeat(totalWidth));

        for (Item item : items) {
            System.out.println(withCategory ? item.toRowWithCategory() : item.toRow());
        }
    }

    // ----------------------------------------------------------------
    // Validated input helpers
    // ----------------------------------------------------------------

    private static String readNonEmptyLine(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = safeReadLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("Input cannot be empty. Please try again.");
        }
    }

    /**
     * Reads a Category (word input only - never a number), re-prompting only
     * the Category field until a valid one is entered. Comparison against
     * Clothing/Electronics/Entertainment is case-insensitive.
     */
    private static String readCategory(String prompt) {
        while (true) {
            String category = readNonEmptyLine(prompt);
            if (manager.isValidCategory(category)) {
                return category;
            }
            System.out.println("Category " + category + " does not exist!");
        }
    }

    /**
     * Reads a valid, unique Item ID: must contain at least one letter and one
     * digit, letters/digits only (no spaces or symbols). Leading zeros are
     * fine since IDs are strings, not numeric fields. Re-prompts the ID field
     * only - both on a format violation and on a duplicate ID.
     */
    private static String readUniqueId(String prompt) {
        while (true) {
            System.out.print(prompt);
            String id = safeReadLine().trim();
            if (!id.matches(ID_FORMAT)) {
                System.out.println("Invalid ID. ID must be 2–20 characters long, contain at least one letter and one number, and use only letters and numbers.");
                continue;
            }
            if (manager.idExists(id)) {
                System.out.println("Item ID already exists.");
                continue;
            }
            return id;
        }
    }

    /**
     * Reads a valid Item Name: letters, numbers, and spaces only. Leading and
     * trailing spaces are trimmed; normal spaces between words are allowed.
     */
    private static String readItemName(String prompt) {
        while (true) {
            System.out.print(prompt);
            String name = safeReadLine().trim();
            if (name.isEmpty() || !name.matches(NAME_FORMAT)) {
                System.out.println("Invalid item name. Only letters, numbers, and spaces are allowed.");
                continue;
            }
            return name;
        }
    }

    /**
     * Reads a word-based choice between exactly two options (case-insensitive),
     * used by Update Item, Sort By, and Sort Order. This is the general
     * replacement for the old numbered sub-menu choices - the main menu is
     * the only place that still accepts numbers.
     */
    private static String readWordChoice(String prompt, String option1, String option2, String invalidMessage) {
        while (true) {
            System.out.print(prompt);
            String line = safeReadLine().trim();
            if (line.equalsIgnoreCase(option1)) {
                return option1.toLowerCase();
            } else if (line.equalsIgnoreCase(option2)) {
                return option2.toLowerCase();
            }
            System.out.println(invalidMessage);
        }
    }

    /**
     * Reads a valid Quantity: a whole number between 1 and 1,000,000
     * inclusive, with no leading zeros, decimals, signs, or scientific
     * notation. The format is checked against INTEGER_FORMAT
     * ("[1-9][0-9]*") BEFORE any conversion is attempted, so
     * Integer.parseInt() only ever runs on a string already known to be a
     * clean positive whole number.
     */
    private static int readQuantity(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = safeReadLine().trim();
            if (!line.matches(INTEGER_FORMAT)) {
                System.out.println("Quantity must be between 0 and 10,000.");
                continue;
            }
            try {
                int value = Integer.parseInt(line);
                if (value < 0 || value > MAX_QUANTITY) {
                    System.out.println("Quantity must be between 0 and 10,000.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Quantity must be between 0 and 10,000.");
            }
        }
    }

    /**
     * Reads a valid Price: a number greater than 0 and at most 1,000,000,
     * whole or decimal, with no leading zeros and no scientific notation.
     *
     * PRICE_FORMAT is checked first so "abc", "10abc", "1e2", "1.2.3", and
     * "01.50"/"0010" (leading zeros) are all rejected before conversion is
     * ever attempted.
     */
    private static double readPrice(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = safeReadLine().trim();
            if (!line.matches(PRICE_FORMAT)) {
                System.out.println("Invalid price. Please enter a valid number.");
                continue;
            }
            double value;
            try {
                value = Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println(OVERFLOW_MESSAGE);
                continue;
            }
            if (Double.isInfinite(value)) {
                System.out.println(OVERFLOW_MESSAGE);
                continue;
            }
            if (value <= 0 || value > MAX_PRICE) {
                System.out.println("Price must be greater than 0 and must not exceed 1,000,000.");
                continue;
            }
            return value;
        }
    }
}