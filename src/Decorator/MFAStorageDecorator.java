package Decorator;

import Interfaces.StorageStrategy;
import Strategy.StorageAPI;
import Interfaces.Passwords;
import Composite.Category;
import java.util.Scanner;
import java.util.LinkedHashMap;
import java.util.Random;

/**
 * A decorator that adds dynamic Multi-Factor Authentication capability
 * by generating a random 6-digit code for user verification.
 */
public class MFAStorageDecorator extends StorageDecorator {
    private Random random = new Random();
    private String lastGeneratedCode = null;

    public MFAStorageDecorator(StorageStrategy storageStrategy) {
        super(storageStrategy);
    }

    
    public String savePassword(Passwords password) {
        if (!authenticateMFA()) {
            System.out.println("MFA authentication failed. Operation cancelled.");
            return null;
        }
        return super.savePassword(password);
    }

    
    public String getPassword(String id) {
        if (!authenticateMFA()) {
            System.out.println("MFA authentication failed. Operation cancelled.");
            return null;
        }
        return super.getPassword(id);
    }

    
    public LinkedHashMap<String, Passwords> getAllPasswords() {
        if (!authenticateMFA()) {
            System.out.println("MFA authentication failed. Operation cancelled.");
            return new LinkedHashMap<>();
        }
        return super.getAllPasswords();
    }

    
    public Category getRootCategory() {
        if (!authenticateMFA()) {
            System.out.println("MFA authentication failed. Operation cancelled.");
            return null;
        }
        return super.getRootCategory();
    }

    public String savePasswordWithCategory(Passwords password, String categoryPath) {
        if (!authenticateMFA()) {
            System.out.println("MFA authentication failed. Operation cancelled.");
            return null;
        }
        return super.savePasswordWithCategory(password, categoryPath);
    }

    /**
     * Generates a random 6-digit code, shows it to the user,
     * and then verifies the user's input against it.
     */
    private boolean authenticateMFA() {
        try {
            // Generate a random 6-digit code
            StringBuilder codeBuilder = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                codeBuilder.append(random.nextInt(10));
            }
            lastGeneratedCode = codeBuilder.toString();

            // Display the code to the user
            System.out.println("=================================================");
            System.out.println("YOUR SECURITY CODE: " + lastGeneratedCode);
            System.out.println("Please enter this code to continue...");
            System.out.println("=================================================");

            // Ask for the code
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter the 6-digit security code: ");
            String userInput = scanner.nextLine().trim();

            // Verify the code
            boolean isValid = userInput.equals(lastGeneratedCode);
            if (!isValid) {
                System.out.println("Invalid security code!");
            }
            return isValid;

        } catch (Exception e) {
            System.out.println("Error during MFA authentication: " + e.getMessage());
            return false;
        }
    }
}