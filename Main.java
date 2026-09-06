import java.util.List;
import java.util.Scanner;


public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final InventoryManager manager = new InventoryManager();

    // A plain positive whole number: no sign, no leading zero, digits only.
    // Rejects letters, decimals, scientific notation (e/E), spaces, symbols,
    // commas, and leading zeros (e.g. "0009") all in one check.
    private static final String INTEGER_FORMAT = "[1-9][0-9]*";

    // A positive number, whole or with a decimal part: "0", "0.5", "10",
    // "10.50" are all allowed, but "01.50", "0010", "1e2", "1.2.3" are not.
    private static final String PRICE_FORMAT = "0(\\.[0-9]+)?|[1-9][0-9]*(\\.[0-9]+)?";

    private static final String OVERFLOW_MESSAGE =
            "Invalid number. Please enter a valid value within the allowed range.";

    // A sane upper bound on how many digits a real store price's whole-number
    // part can have. Blocks the case where Double.parseDouble() would NOT
    // throw (a 50+ digit number is still a legal, finite double) but the
    // value is clearly not a realistic price - without this check such input
    // would silently be accepted instead of being caught as an overflow.
    private static final int MAX_PRICE_INTEGER_DIGITS = 12;

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
     * Reads the main menu choice. On any invalid input (letters, decimals,
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
        String category = readNonEmptyLine("Enter Category (Clothing / Electronics / Entertainment): ");
        if (!manager.isValidCategory(category)) {
            System.out.println("Category " + category + " does not exist!");
            return;
        }

        String id = readUniqueId("Enter ID: ");
        String name = readNonEmptyLine("Enter Name: ");
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

        System.out.println("1 - Quantity");
        System.out.println("2 - Price");
        int fieldChoice = readTwoOptionChoice("Enter choice: ", "Invalid update option.");

        // The old value is only ever read here, BEFORE the new value is validated.
        // item.setQuantity()/setPrice() is only called once readQuantity()/readPrice()
        // has already returned a fully valid value, so an invalid entry can never
        // corrupt the existing data (Data Integrity requirement).
        if (fieldChoice == 1) {
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

        String category = readNonEmptyLine("Enter Category: ");
        if (!manager.isValidCategory(category)) {
            System.out.println("Category " + category + " does not exist!");
            return;
        }
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

        System.out.println("Sort by:");
        System.out.println("1 - Quantity");
        System.out.println("2 - Price");
        int fieldChoice = readTwoOptionChoice("Enter choice: ", "Invalid sort option.");
        String sortBy = (fieldChoice == 1) ? "quantity" : "price";

        System.out.println("Sort order:");
        System.out.println("1 - Ascending");
        System.out.println("2 - Descending");
        int orderChoice = readTwoOptionChoice("Enter choice: ", "Invalid sort order.");
        String order = (orderChoice == 1) ? "ascending" : "descending";

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

    private static void printTable(List<Item> items, boolean withCategory, String emptyMessage) {
        if (items.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        if (withCategory) {
            System.out.printf("%-10s %-20s %-10s %-10s %-15s%n", "ID", "Name", "Quantity", "Price", "Category");
        } else {
            System.out.printf("%-10s %-20s %-10s %-10s%n", "ID", "Name", "Quantity", "Price");
        }
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

    private static String readUniqueId(String prompt) {
        while (true) {
            String id = readNonEmptyLine(prompt);
            if (manager.idExists(id)) {
                System.out.println("Item ID already exists.");
                continue;
            }
            return id;
        }
    }

    /**
     * Reads a 1-or-2 choice used by the Update-field and Sort-field/order
     * sub-menus. Format is validated the same way as the main menu (String
     * check before parsing), so letters, decimals, signs, leading zeros,
     * scientific notation, and overflow-sized numbers are all rejected
     * without ever throwing.
     */
    private static int readTwoOptionChoice(String prompt, String invalidMessage) {
        while (true) {
            System.out.print(prompt);
            String line = safeReadLine().trim();

            if (line.matches(INTEGER_FORMAT)) {
                try {
                    int value = Integer.parseInt(line);
                    if (value == 1 || value == 2) {
                        return value;
                    }
                } catch (NumberFormatException e) {
                    System.out.println(OVERFLOW_MESSAGE);
                    continue;
                }
            }
            System.out.println(invalidMessage);
        }
    }

    /**
     * Reads a valid Quantity: a positive whole number with no leading zeros.
     *
     * The format is checked against INTEGER_FORMAT ("[1-9][0-9]*") BEFORE any
     * conversion is attempted. That single regex already rejects zero,
     * negative numbers, decimals, letters, symbols, commas, spaces inside the
     * number, scientific notation (1e2), and leading zeros (0009) - so
     * Integer.parseInt() only ever runs on a string that is already known to
     * be a clean positive whole number. The surrounding try/catch only
     * remains to guard against an extremely large number of digits (e.g. 30
     * nines) that would otherwise overflow int and crash the program.
     */
    private static int readQuantity(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = safeReadLine().trim();
            if (!line.matches(INTEGER_FORMAT)) {
                System.out.println("Quantity must be a positive whole number without leading zeros.");
                continue;
            }
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println(OVERFLOW_MESSAGE);
            }
        }
    }

    /**
     * Reads a valid Price: a positive number, whole or decimal, with no
     * leading zeros and no scientific notation.
     *
     * PRICE_FORMAT is checked first so "abc", "10abc", "1e2", "1.2.3", and
     * "01.50"/"0010" (leading zeros) are all rejected before conversion is
     * ever attempted. A value of exactly 0 (e.g. "0" or "0.00") passes the
     * format check but is rejected separately with its own message, matching
     * the two distinct error messages required for Price.
     */
    private static double readPrice(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = safeReadLine().trim();
            if (!line.matches(PRICE_FORMAT)) {
                System.out.println("Invalid price. Please enter a valid number.");
                continue;
            }
            String integerPart = line.contains(".") ? line.substring(0, line.indexOf('.')) : line;
            if (integerPart.length() > MAX_PRICE_INTEGER_DIGITS) {
                System.out.println(OVERFLOW_MESSAGE);
                continue;
            }
            double value;
            try {
                value = Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println(OVERFLOW_MESSAGE);
                continue;
            }
            if (value == 0 || Double.isInfinite(value)) {
                System.out.println("Price must be greater than 0.");
                continue;
            }
            return value;
        }
    }
}