import Interfaces.StorageImplementation;
import Interfaces.Passwords;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.UUID;

public class StorageAPI implements StorageImplementation {
    private static final String FILE_PATH = "passwords.csv"; // File to store passwords
    private LinkedHashMap<String, Passwords> storage = new LinkedHashMap<>();

    @Override
    public String getStorage(String storageId) {
        return storage.get(storageId) != null ? storage.get(storageId).getPassword() : null;
    }

    public String setStorage(Passwords password) {
        String id = UUID.randomUUID().toString();
        storage.put(id, password);
        saveToFile(password, id);
        return id;
    }

    private void saveToFile(Passwords password, String id) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(id + "," + password.getName() + "," + password.getUsername() + "," + password.getPassword());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String id = parts[0];
                    String name = parts[1];
                    String username = parts[2];
                    String passwordValue = parts[3];

                    Standart password = new Standart();
                    password.setName(name);
                    password.setUsername(username);
                    password.setPassword(passwordValue);

                    storage.put(id, password);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all stored passwords as a map.
     * @return a map of password IDs to Passwords objects
     */
    public LinkedHashMap<String, Passwords> getAllPasswords() {
        return storage;
    }
}
