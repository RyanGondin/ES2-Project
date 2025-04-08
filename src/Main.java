import Interfaces.Passwords;
import Strategy.StorageAPI;
import Strategy.FileStorageStrategy;
import Exceptions.UndefinedPasswordException;
import Factory.FactoryPassword;
import Interfaces.PasswordType;
import Memento.MementoOriginator;

import java.util.Scanner;
import java.util.LinkedHashMap;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            // Check if password files exist
            File passwordsFile = new File("passwords.bin");
            File categoriesFile = new File("categories.bin");
            boolean filesExist = passwordsFile.exists() && categoriesFile.exists();
            
            // First create storage API with empty password
            StorageAPI api = new StorageAPI("");
            
            // If files exist, we need to prompt for the correct password
            if (filesExist) {
                System.out.println("Password manager files found. Please enter your master password:");
                String masterPassword = readPasswordFromConsole();
                
                // Update the master password but don't save yet
                api.setMasterPassword(masterPassword);
                
                // If using the FileStorageStrategy, explicitly load data with password
                if (api.getStorageStrategy() instanceof FileStorageStrategy) {
                    ((FileStorageStrategy)api.getStorageStrategy()).loadDataWithMasterPassword(masterPassword);
                }
            } else {
                // New user, prompt to create a master password
                System.out.println("No master password set. Please set a master password:");
                String newPassword = readPasswordFromConsole();
                System.out.println("Confirm the master password:");
                String confirmPassword = readPasswordFromConsole();
                
                if (newPassword.equals(confirmPassword)) {
                    api.setMasterPassword(newPassword);
                    System.out.println("Master password set successfully!");
                } else {
                    System.out.println("Passwords don't match. Exiting.");
                    return;
                }
            }
            
            // Continue with the rest of your application
            showMainMenu(api);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static String readPasswordFromConsole() {
        return scanner.nextLine();
    }

    private static void showMainMenu(StorageAPI storageAPI) {
        // Main menu loop
        while (true) {
            System.out.println("\n=== Password Manager ===");
            System.out.println("1. Create a Strong Password");
            System.out.println("2. Create a Standart Password");
            System.out.println("3. Store a Password");
            System.out.println("4. Retrieve a Password by ID");
            System.out.println("5. Display All Stored Passwords");
            System.out.println("6. Manage Categories");
            System.out.println("7. Restore Previous State");
            System.out.println("8. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> createPassword(scanner, PasswordType.STRONG);
                case 2 -> createPassword(scanner, PasswordType.STANDART);
                case 3 -> storePassword(storageAPI);
                case 4 -> retrievePassword(scanner, storageAPI);
                case 5 -> displayAllPasswords(storageAPI);
                case 6 -> manageCategories(scanner, storageAPI);
                case 7 -> restorePreviousState(scanner, storageAPI, new MementoOriginator());
                case 8 -> {
                    System.out.println("Exiting Password Manager. Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void createPassword(Scanner scanner, PasswordType type) {
        try {
            Passwords password = FactoryPassword.makePassword(type);
            System.out.println("Generated Password: " + password.getPassword());
        } catch (UndefinedPasswordException e) {
            System.out.println("Error creating password: " + e.getMessage());
        }
    }

    private static void storePassword(StorageAPI storageAPI) {
        System.out.println("Enter the name of the service (e.g., Email, Bank):");
        String name = scanner.nextLine();
        
        System.out.println("Enter the username:");
        String username = scanner.nextLine();
        
        System.out.println("Do you want to create a password or input one manually?");
        System.out.println("1. Create a password");
        System.out.println("2. Input a password manually");
        
        int choice = getIntInput(1, 2);
        String password;
        Passwords pwd = null;
        
        if (choice == 1) {
            System.out.println("Choose the type of password to create:");
            System.out.println("1. Strong Password");
            System.out.println("2. Standart Password");
            int typeChoice = getIntInput(1, 2);
            
            try {
                if (typeChoice == 1) {
                    pwd = FactoryPassword.makePassword(PasswordType.STRONG);
                    pwd.setName(name);
                    pwd.setUsername(username);
                    password = pwd.getPassword(); // Get the generated password
                    System.out.println("Generated Password: " + password);
                } else {
                    pwd = FactoryPassword.makePassword(PasswordType.STANDART);
                    pwd.setName(name);
                    pwd.setUsername(username);
                    password = pwd.getPassword(); // Get the generated password
                    System.out.println("Generated Password: " + password);
                }
            } catch (Exception e) {
                System.out.println("Error creating password: " + e.getMessage());
                return;
            }
        } else {
            System.out.println("Enter the password:");
            password = scanner.nextLine();
            
            // For manual entry, create a Standart password
            try {
                pwd = FactoryPassword.makePassword(PasswordType.STANDART);
                pwd.setName(name);
                pwd.setUsername(username);
                pwd.setPassword(password);
            } catch (Exception e) {
                System.out.println("Error creating password: " + e.getMessage());
                return;
            }
        }
        
        // Ask if they want to add to a category
        System.out.println("Do you want to add this password to a category?");
        System.out.println("1. Yes");
        System.out.println("2. No");
        int categoryChoice = getIntInput(1, 2);
        
        if (categoryChoice == 1) {
            // Show category hierarchy to help
            displayCategoryHierarchy(storageAPI);
            System.out.println("Enter category path (e.g., Work/Email):");
            String categoryPath = scanner.nextLine();
            
            String passwordId = storageAPI.savePasswordWithCategory(pwd, categoryPath);
            System.out.println("Password stored successfully! ID: " + passwordId);
        } else {
            // Store without category assignment
            String passwordId = storageAPI.savePassword(pwd);
            System.out.println("Password stored without category! ID: " + passwordId);
        }
    }

    private static void retrievePassword(Scanner scanner, StorageAPI storageAPI) {
        System.out.print("Enter the ID of the password to retrieve: ");
        String id = scanner.nextLine();

        LinkedHashMap<String, Passwords> allPasswords = storageAPI.getAllPasswords();
        Passwords password = allPasswords.get(id);

        if (password != null) {
            System.out.println("\nRetrieved Password Information:");
            System.out.println("Service Name: " + password.getName());
            System.out.println("Username: " + password.getUsername());
            System.out.println("Password: " + password.getPassword());
        } else {
            System.out.println("No password found with the given ID.");
        }
    }

    private static void displayAllPasswords(StorageAPI storageAPI) {
        LinkedHashMap<String, Passwords> allPasswords = storageAPI.getAllPasswords();

        if (allPasswords.isEmpty()) {
            System.out.println("No passwords stored.");
        } else {
            System.out.println("\n=== Stored Passwords ===");
            allPasswords.forEach((id, password) -> {
                System.out.println("ID: " + id);
                System.out.println("Service Name: " + password.getName());
                System.out.println("Username: " + password.getUsername());
                System.out.println("Password: " + password.getPassword());
                System.out.println("------------------------");
            });
        }
    }

    private static void manageCategories(Scanner scanner, StorageAPI storageAPI) {
        while (true) {
            System.out.println("\n=== Category Management ===");
            System.out.println("1. Display Category Hierarchy");
            System.out.println("2. Create New Category");
            System.out.println("3. Add Password to Category");
            System.out.println("4. Back to Main Menu");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            switch (choice) {
                case 1 -> storageAPI.displayCategoryHierarchy();
                case 2 -> {
                    System.out.print("Enter category path (e.g., Work/Email): ");
                    String path = scanner.nextLine();
                    storageAPI.addPasswordToCategory(path, null); // Just create the category
                    System.out.println("Category created!");
                }
                case 3 -> {
                    System.out.print("Enter password ID: ");
                    String passwordId = scanner.nextLine();
                    System.out.print("Enter category path: ");
                    String categoryPath = scanner.nextLine();
                    
                    Passwords password = storageAPI.getAllPasswords().get(passwordId);
                    if (password != null) {
                        storageAPI.addPasswordToCategory(categoryPath, password);
                        System.out.println("Password added to category!");
                    } else {
                        System.out.println("Password not found!");
                    }
                }
                case 4 -> {
                    return;
                }
            }
        }
    }

    private static void restorePreviousState(Scanner scanner, StorageAPI storageAPI, MementoOriginator memento) {
        List<LocalDateTime> timestamps = storageAPI.getCaretaker().getMementoTimestamps();
        
        if (timestamps.isEmpty()) {
            System.out.println("No previous states available to restore.");
            return;
        }
        
        System.out.println("\n=== Available States ===");
        for (int i = 0; i < timestamps.size(); i++) {
            System.out.println((i + 1) + ". " + timestamps.get(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        
        System.out.print("Enter the number of the state to restore (0 to cancel): ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        
        if (choice > 0 && choice <= timestamps.size()) {
            memento.restoreFromMemento(storageAPI.getCaretaker().getMemento(choice - 1));
            System.out.println("State restored successfully!");
        } else if (choice != 0) {
            System.out.println("Invalid selection.");
        }
    }

    private static int getIntInput(int min, int max) {
        int input;
        while (true) {
            try {
                String line = scanner.nextLine();
                input = Integer.parseInt(line);
                if (input >= min && input <= max) {
                    return input;
                } else {
                    System.out.println("Please enter a number between " + min + " and " + max);
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number");
            }
        }
    }

    private static void displayCategoryHierarchy(StorageAPI storageAPI) {
        System.out.println("\n=== Category Hierarchy ===");
        storageAPI.displayCategoryHierarchy();
    }

}
