import Interfaces.Passwords;
import Interfaces.StorageStrategy;
import Strategy.StorageAPI;
import Strategy.FileStorageStrategy;
import Decorator.MFAStorageDecorator;
import Decorator.AlertStorageDecorator;
import Exceptions.UndefinedPasswordException;
import Factory.FactoryPassword;
import Interfaces.PasswordType;
import Memento.MementoOriginator;
import Interfaces.UserIO;
import Interfaces.ConsoleUserIO;
import Memento.PasswordManagerMemento;

import java.util.Scanner;
import java.util.LinkedHashMap;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            // Check if password files exist
            File passwordsFile = new File("passwords.bin");
            File categoriesFile = new File("categories.bin");
            boolean filesExist = passwordsFile.exists() && categoriesFile.exists();

            // Create the base storage API
            StorageAPI baseStorage = new StorageAPI("");

            // Compose decorators dynamically
            UserIO userIO = new ConsoleUserIO();
            StorageStrategy storage = composeDecorators(baseStorage, userIO);

            // Create Memento originator
            MementoOriginator originator = new MementoOriginator();

            // If files exist, prompt for the master password
            if (filesExist) {
                System.out.println("Password manager files found. Please enter your master password:");
                String masterPassword = readPasswordFromConsole();

                // Set the master password and load data
                baseStorage.setMasterPassword(masterPassword);
                if (baseStorage.getStorageStrategy() instanceof FileStorageStrategy fileStorageStrategy) {
                    fileStorageStrategy.loadDataWithMasterPassword(masterPassword);
                }
            } else {
                // Prompt the user to create a new master password
                System.out.println("No master password set. Please set a master password:");
                String newPassword = readPasswordFromConsole();
                System.out.println("Confirm the master password:");
                String confirmPassword = readPasswordFromConsole();

                if (newPassword.equals(confirmPassword)) {
                    baseStorage.setMasterPassword(newPassword);
                    System.out.println("Master password set successfully!");
                } else {
                    System.out.println("Passwords don't match. Exiting.");
                    return;
                }
            }

            // Pass the decorated storage and originator to the main menu
            showMainMenu(storage, originator);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    /**
     * Composes decorators dynamically based on runtime conditions.
     *
     * @param baseStorage The base storage API.
     * @param userIO      The user input/output interface.
     * @return The decorated storage strategy.
     */
    private static StorageStrategy composeDecorators(StorageAPI baseStorage, UserIO userIO) {
    // Start with the base storage
        StorageStrategy storage = baseStorage;

        // Add MFA decorator
        storage = new MFAStorageDecorator(storage, userIO);

        // Add Alert decorator
        storage = new AlertStorageDecorator(storage);

        return storage;
    }

    private static String readPasswordFromConsole() {
        return scanner.nextLine();
    }

    private static void showMainMenu(StorageStrategy storage, MementoOriginator originator) {
        while (true) {
            System.out.println("\n=== Password Manager ===");
            System.out.println("1. Create a Strong Password");
            System.out.println("2. Create a Standard Password");
            System.out.println("3. Store a Password");
            System.out.println("4. Retrieve a Password by ID");
            System.out.println("5. Display All Stored Passwords");
            System.out.println("6. Save Current State");
            System.out.println("7. Restore Previous State");
            System.out.println("8. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> createPassword(storage, PasswordType.STRONG);
                case 2 -> createPassword(storage, PasswordType.STANDART);
                case 3 -> storePassword(storage);
                case 4 -> retrievePassword(storage);
                case 5 -> displayAllPasswords(storage);
                case 6 -> saveCurrentState(originator);
                case 7 -> restorePreviousState(originator);
                case 8 -> {
                    System.out.println("Exiting Password Manager. Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void createPassword(StorageStrategy storage, PasswordType type) {
        try {
            Passwords password = FactoryPassword.makePassword(type);
            System.out.println("Generated Password: " + password.getPassword());
        } catch (UndefinedPasswordException e) {
            System.out.println("Error creating password: " + e.getMessage());
        }
    }

    private static void storePassword(StorageStrategy storage) {
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
                } else {
                    pwd = FactoryPassword.makePassword(PasswordType.STANDART);
                }
                pwd.setName(name);
                pwd.setUsername(username);
                password = pwd.getPassword();
                System.out.println("Generated Password: " + password);
            } catch (Exception e) {
                System.out.println("Error creating password: " + e.getMessage());
                return;
            }
        } else {
            System.out.println("Enter the password:");
            password = scanner.nextLine();

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

        System.out.println("Do you want to add this password to a category?");
        System.out.println("1. Yes");
        System.out.println("2. No");
        int categoryChoice = getIntInput(1, 2);

        if (categoryChoice == 1) {
            System.out.println("Enter category path (e.g., Work/Email):");
            String categoryPath = scanner.nextLine();
            String passwordId = storage.savePasswordWithCategory(pwd, categoryPath);
            System.out.println("Password stored successfully! ID: " + passwordId);
        } else {
            String passwordId = storage.savePassword(pwd);
            System.out.println("Password stored successfully! ID: " + passwordId);
        }
    }

    private static void retrievePassword(StorageStrategy storage) {
        System.out.print("Enter the ID of the password to retrieve: ");
        String id = scanner.nextLine();

        LinkedHashMap<String, Passwords> allPasswords = storage.getAllPasswords();
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

    private static void displayAllPasswords(StorageStrategy storage) {
        LinkedHashMap<String, Passwords> allPasswords = storage.getAllPasswords();

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

    private static void saveCurrentState(MementoOriginator originator) {
        originator.saveState();
        System.out.println("Current state saved successfully.");
    }

    private static void restorePreviousState(MementoOriginator originator) {
        List<LocalDateTime> timestamps = originator.getMementoTimestamps();
        if (timestamps.isEmpty()) {
            System.out.println("No saved states available.");
            return;
        }

        System.out.println("Available saved states:");
        for (int i = 0; i < timestamps.size(); i++) {
            System.out.println((i + 1) + ". " + timestamps.get(i));
        }

        System.out.print("Choose a state to restore: ");
        int choice = getIntInput(1, timestamps.size());
        PasswordManagerMemento memento = originator.getMemento(choice - 1);

        if (memento != null) {
            originator.restoreFromMemento(memento);
            System.out.println("State restored successfully.");
        } else {
            System.out.println("Invalid choice. No state restored.");
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
}
