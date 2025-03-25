import Interfaces.Passwords;
import Exceptions.UndefinedPasswordException;
import Interfaces.PasswordType;
import Interfaces.StorageImplementation;

import java.util.Scanner;
import java.util.LinkedHashMap;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        StorageImplementation localStorage = new StorageAPI();
        StorageManager storageManager = StorageManagerFactory.getInstance(localStorage, false); // Use StorageRequest

        while (true) {
            System.out.println("\n=== Password Manager ===");
            System.out.println("1. Create a Strong Password");
            System.out.println("2. Create a Standard Password");
            System.out.println("3. Store a Password");
            System.out.println("4. Retrieve a Password by ID");
            System.out.println("5. Display All Stored Passwords");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> createPassword(scanner, PasswordType.STRONG);
                case 2 -> createPassword(scanner, PasswordType.STANDART);
                case 3 -> storePassword(scanner, storageManager);
                case 4 -> retrievePassword(scanner, storageManager);
                case 5 -> displayAllPasswords(storageManager);
                case 6 -> {
                    System.out.println("Exiting Password Manager. Goodbye!");
                    scanner.close();
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

    private static void storePassword(Scanner scanner, StorageManager storageManager) {
        System.out.print("Enter the name of the service (e.g., Email, Bank): ");
        String name = scanner.nextLine();

        System.out.print("Enter the username: ");
        String username = scanner.nextLine();

        System.out.println("Do you want to create a password or input one manually?");
        System.out.println("1. Create a password");
        System.out.println("2. Input a password manually");
        System.out.print("Choose an option: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        String passwordValue = null;

        try {
            if (choice == 1) {
                System.out.println("Choose the type of password to create:");
                System.out.println("1. Strong Password");
                System.out.println("2. Standard Password");
                System.out.print("Choose an option: ");
                int passwordTypeChoice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                PasswordType type = (passwordTypeChoice == 1) ? PasswordType.STRONG : PasswordType.STANDART;
                Passwords password = FactoryPassword.makePassword(type);
                passwordValue = password.getPassword();
                System.out.println("Generated Password: " + passwordValue);
            } else if (choice == 2) {
                System.out.print("Enter the password: ");
                passwordValue = scanner.nextLine();
            } else {
                System.out.println("Invalid choice. Returning to the main menu.");
                return;
            }

            Passwords password = FactoryPassword.makePassword(PasswordType.STRONG); // Default to strong
            password.setName(name);
            password.setUsername(username);
            password.setPassword(passwordValue);

            // Cast storageManager to StorageRequest to access the correct setStorage method
            if (storageManager instanceof StorageRequest) {
                StorageRequest storageRequest = (StorageRequest) storageManager;
                String id = storageRequest.storageImplementation.setStorage(password);
                System.out.println("Password stored successfully! ID: " + id);
            } else {
                System.out.println("Storage manager is not compatible.");
            }
        } catch (UndefinedPasswordException e) {
            System.out.println("Error storing password: " + e.getMessage());
        }
    }

    private static void retrievePassword(Scanner scanner, StorageManager storageManager) {
        System.out.print("Enter the ID of the password to retrieve: ");
        String id = scanner.nextLine();

        try {
            String password = storageManager.getStorage(id);
            if (password != null) {
                System.out.println("Retrieved Password: " + password);
            } else {
                System.out.println("No password found with the given ID.");
            }
        } catch (Exception e) {
            System.out.println("Error retrieving password: " + e.getMessage());
        }
    }

    private static void displayAllPasswords(StorageManager storageManager) {
        if (storageManager instanceof StorageRequest) {
            StorageImplementation storageImplementation = ((StorageRequest) storageManager).storageImplementation;
            if (storageImplementation instanceof StorageAPI) {
                StorageAPI storageAPI = (StorageAPI) storageImplementation;
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
            } else {
                System.out.println("Storage implementation does not support retrieving all passwords.");
            }
        } else {
            System.out.println("Storage manager is not compatible.");
        }
    }
}
