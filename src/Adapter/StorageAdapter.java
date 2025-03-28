package Adapter;
import Interfaces.StorageImplementation;
import Interfaces.Passwords;
import Factory.Standart; // Add this import
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.UUID;

public class StorageAdapter implements StorageImplementation {
    private final FileStorage fileStorage;
    private final Encryptor encryptor;
    private LinkedHashMap<String, Passwords> storage;

    public StorageAdapter(FileStorage fileStorage, String masterPassword, boolean loadStorage) {
        this.fileStorage = fileStorage;
        this.encryptor = new Encryptor(masterPassword);
        this.storage = new LinkedHashMap<>();
        if (loadStorage) {
            loadStorage();
        }
    }

    // Keep the old constructor for compatibility
    public StorageAdapter(FileStorage fileStorage, String masterPassword) {
        this(fileStorage, masterPassword, true);
    }

    @Override
    public String getStorage(String storageId) {
        return storage.get(storageId) != null ? storage.get(storageId).getPassword() : null;
    }

    @Override
    public String setStorage(Passwords password) {
        String id = UUID.randomUUID().toString();
        storage.put(id, password);
        saveStorage();
        return id;
    }

    public LinkedHashMap<String, Passwords> getAllPasswords() {
        return new LinkedHashMap<>(storage);
    }

    private void loadStorage() {
        try {
            if (fileStorage.exists()) {
                String data = fileStorage.readData();
                if (!data.trim().isEmpty()) {
                    try {
                        String decryptedData;
                        // Handle versioning
                        if (data.startsWith("v2:")) {
                            // Current version (CBC mode)
                            decryptedData = encryptor.decrypt(data.substring(3));
                        } else {
                            // Legacy format (no version prefix)
                            throw new IOException("Incompatible encryption format");
                        }
                        deserializePasswords(decryptedData);
                    } catch (Exception e) {
                        System.err.println("Encryption error details: ");
                        e.printStackTrace();
                        System.err.println("Creating empty password storage due to decryption error.");
                        storage = new LinkedHashMap<>();
                    }
                }
            } else {
                fileStorage.create();
            }
        } catch (IOException e) {
            System.err.println("Error loading storage: " + e.getMessage());
        }
    }

    private void saveStorage() {
        try {
            String serializedData = serializePasswords();
            // Add version prefix to the encrypted data
            String encryptedData = "v2:" + encryptor.encrypt(serializedData);
            fileStorage.writeData(encryptedData);
        } catch (IOException e) {
            System.err.println("Error saving storage: " + e.getMessage());
        }
    }

    private String serializePasswords() {
        StringBuilder builder = new StringBuilder();
        for (var entry : storage.entrySet()) {
            Passwords password = entry.getValue();
            builder.append(String.format("%s,%s,%s,%s\n",
                entry.getKey(),
                password.getName(),
                password.getUsername(),
                password.getPassword()));
        }
        return builder.toString();
    }

    private void deserializePasswords(String data) {
        storage.clear();
        String[] lines = data.split("\n");
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    Standart password = new Standart();
                    password.setName(parts[1]);
                    password.setUsername(parts[2]);
                    password.setPassword(parts[3]);
                    storage.put(parts[0], password);
                }
            }
        }
    }
}