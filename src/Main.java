import Interfaces.Passwords;
import Interfaces.StorageAPI;
import Exceptions.UndefinedPasswordException;
import Factory.FactoryPassword;
import Interfaces.PasswordType;

import java.util.Scanner;
import java.util.LinkedHashMap;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Don't initialize with null - just declare the variable
        StorageAPI storageAPI;

        try {
            File masterPasswordFile = new File("master_password.bin");
            // Initialize with empty password but skip loading storage
            storageAPI = new StorageAPI("", false);
            
            if (!masterPasswordFile.exists()) {
                System.out.println("No master password set. Please set a master password:");
                System.out.print("Enter a new master password: ");
                String newMasterPassword = scanner.nextLine();
                System.out.print("Confirm the master password: ");
                String confirmPassword = scanner.nextLine();

                if (!newMasterPassword.equals(confirmPassword)) {
                    System.out.println("Passwords do not match. Exiting...");
                    return;
                }

                storageAPI.setMasterPassword(newMasterPassword);
                System.out.println("Master password set successfully!");
            }

            // Prompt the user to enter the master password to unlock the password manager
            System.out.print("Enter your master password to unlock the password manager: ");
            String masterPassword = scanner.nextLine();

            if (!storageAPI.verifyMasterPassword(masterPassword)) {
                System.out.println("Incorrect master password. Exiting...");
                return;
            }

            System.out.println("Password manager unlocked!");

            // Now initialize with the actual master password for proper encryption/decryption
            storageAPI = new StorageAPI(masterPassword);

            // Main menu loop
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
                    case 3 -> storePassword(scanner, storageAPI);
                    case 4 -> retrievePassword(scanner, storageAPI);
                    case 5 -> displayAllPasswords(storageAPI);
                    case 6 -> {
                        System.out.println("Exiting Password Manager. Goodbye!");
                        scanner.close();
                        return;
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
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

    private static void storePassword(Scanner scanner, StorageAPI storageAPI) {
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

            String id = storageAPI.setStorage(password);
            System.out.println("Password stored successfully! ID: " + id);
        } catch (UndefinedPasswordException e) {
            System.out.println("Error storing password: " + e.getMessage());
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
}
