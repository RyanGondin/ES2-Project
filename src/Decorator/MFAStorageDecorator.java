package Decorator;

import Interfaces.StorageStrategy;
import Interfaces.Passwords;
import Composite.Category;
import Interfaces.UserIO;

import java.util.LinkedHashMap;
import java.util.Random;
import java.util.function.Supplier;

public class MFAStorageDecorator extends StorageDecorator {
    private final Random random = new Random();
    private final UserIO userIO;

    public MFAStorageDecorator(StorageStrategy storageStrategy, UserIO userIO) {
        super(storageStrategy);
        this.userIO = userIO;
    }

    private <T> T performWithMFA(Supplier<T> action) {
        if (!authenticateMFA()) {
            userIO.displayMessage("MFA authentication failed. Operation cancelled.");
            return null;
        }
        return action.get();
    }

    @Override
    public String savePassword(Passwords password) {
        return performWithMFA(() -> super.savePassword(password));
    }

    @Override
    public String getPassword(String id) {
        return performWithMFA(() -> super.getPassword(id));
    }

    @Override
    public LinkedHashMap<String, Passwords> getAllPasswords() {
        return performWithMFA(super::getAllPasswords);
    }

    @Override
    public Category getRootCategory() {
        return performWithMFA(super::getRootCategory);
    }

    @Override
    public String savePasswordWithCategory(Passwords password, String categoryPath) {
        return performWithMFA(() -> savePasswordWithCategory(password, categoryPath));
    }

    @Override
    public String PasswordCategory(String passwordId) {
        return performWithMFA(() -> super.PasswordCategory(passwordId));
    }

    private boolean authenticateMFA() {
        try {
            String code = generateSecurityCode();
            userIO.displayMessage("YOUR SECURITY CODE: " + code);
            String userInput = userIO.getInput("Enter the 6-digit security code: ");
            return verifyCode(userInput, code);
        } catch (Exception e) {
            userIO.displayMessage("Error during MFA authentication: " + e.getMessage());
            return false;
        }
    }

    private String generateSecurityCode() {
        StringBuilder codeBuilder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            codeBuilder.append(random.nextInt(10));
        }
        return codeBuilder.toString();
    }

    private boolean verifyCode(String userInput, String code) {
        boolean isValid = userInput.equals(code);
        if (!isValid) {
            userIO.displayMessage("Invalid security code!");
        }
        return isValid;
    }
}