import Interfaces.Passwords;
import Exceptions.UndefinedPasswordException;
import Interfaces.PasswordType;
import Interfaces.StorageImplementation;
import Interfaces.StorageManager;

public class Main {
    public static void main(String[] args) {
        try {
            Passwords strongPassword = FactoryPassword.makePassword(PasswordType.STRONG);
            strongPassword.setPassword("StrongPass123");
            strongPassword.show(); // Expected output: Password: StrongPass123

            Passwords standartPassword = FactoryPassword.makePassword(PasswordType.STANDART);
            standartPassword.setPassword("StandartPass123");
            standartPassword.show(); // Expected output: Password: StandartPass123
        } catch (UndefinedPasswordException e) {
            e.printStackTrace();
        }

        // Use StorageAPI as the implementation
        StorageImplementation localStorage = new StorageAPI();
        StorageManager storageManager = new StorageRequest(localStorage);

        // Store and retrieve data
        String storageId = storageManager.setStorage("MyPassword123");
        System.out.println("Stored ID: " + storageId);

        String retrievedData = storageManager.getStorage(storageId);
        System.out.println("Retrieved Data: " + retrievedData);

    }
}