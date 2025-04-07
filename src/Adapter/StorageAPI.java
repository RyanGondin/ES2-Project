package Adapter;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashMap;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import Composite.Category;
import Interfaces.FileStorage;
import Interfaces.Passwords;
import Interfaces.StorageImplementation;
import Memento.PasswordManagerCaretaker;
import Memento.PasswordManagerMemento;
import Interfaces.MementoOriginator;

public class StorageAPI implements StorageImplementation {
    private final StorageAdapter adapter;
    private static final boolean LOAD_STORAGE = true; 
    private String lastAccessedPasswordId;
    private PasswordManagerCaretaker caretaker = new PasswordManagerCaretaker(10);
    private final String masterPassword; // 🔹 Armazena a senha mestre

    public StorageAPI(String masterPassword) {
        this(masterPassword, LOAD_STORAGE);
    }

    public StorageAPI(String masterPassword, boolean loadStorage) {
        this.masterPassword = masterPassword; // 🔹 Define a senha mestre
        FileStorage fileStorage = loadStorage ? new FileStorageImpl("passwords.csv") : new FileStorageImpl("dummy.tmp");
        this.adapter = new StorageAdapter(fileStorage, masterPassword, loadStorage);
    }

    // 🔹 Método correto para retornar a senha mestre
    public String getMasterPassword() {
        return this.masterPassword;
    }

    @Override
    public String getStorage(String storageId) {
        this.lastAccessedPasswordId = storageId;
        saveState(); 
        return adapter.getStorage(storageId);
    }

    @Override
    public String setStorage(Passwords password) {
        String id = adapter.setStorage(password);
        this.lastAccessedPasswordId = id;
        saveState(); 
        return id;
    }

    public LinkedHashMap<String, Passwords> getAllPasswords() {
        return adapter.getAllPasswords();
    }
        
    // Create a memento capturing current state
    public PasswordManagerMemento createMemento() {
        return new PasswordManagerMemento(
            adapter.getAllPasswords(), 
            adapter.getRootCategory(),
            lastAccessedPasswordId
        );
    }

    // Restore state from a memento
    public void restoreFromMemento(PasswordManagerMemento memento) {
        if (memento != null) {
            // Now we're using the interface methods
            MementoOriginator originator = memento;
            adapter.restoreState(
                originator.getSavedPasswords(),
                originator.getSavedRootCategory()
            );
            this.lastAccessedPasswordId = originator.getLastAccessedPasswordId();
        }
    }

    // Save state after important operations
    private void saveState() {
        PasswordManagerMemento memento = createMemento();
        caretaker.addMemento(memento);
    }

    public void setMasterPassword(String password) throws IOException {
        // Create a file to store the hashed password and salt
        FileStorage masterPasswordStorage = new FileStorageImpl("master_password.bin");
        FileStorage masterSaltStorage = new FileStorageImpl("master_salt.bin");
        
        // Generate a random salt for the master password
        byte[] salt = new byte[16];
        try {
            SecureRandom.getInstanceStrong().nextBytes(salt);
        } catch (NoSuchAlgorithmException e) {
            new SecureRandom().nextBytes(salt);
        }
        
        // Store the salt
        masterSaltStorage.writeData(Base64.getEncoder().encodeToString(salt));
        
        // Hash the password with the salt
        String hashedPassword = hashPassword(password, salt);
        masterPasswordStorage.writeData(hashedPassword);
    }

    public boolean verifyMasterPassword(String password) throws IOException {
        FileStorage masterPasswordStorage = new FileStorageImpl("master_password.bin");
        FileStorage masterSaltStorage = new FileStorageImpl("master_salt.bin");
        
        if (!masterPasswordStorage.exists() || !masterSaltStorage.exists()) {
            return false;
        }
        
        // Get the stored salt
        String encodedSalt = masterSaltStorage.readData().trim();
        byte[] salt = Base64.getDecoder().decode(encodedSalt);
        
        // Hash the input password with the same salt
        String storedHash = masterPasswordStorage.readData().trim();
        String inputHash = hashPassword(password, salt);
        
        // Compare the hashes
        return storedHash.equals(inputHash);
    }

    private String hashPassword(String password, byte[] salt) {
        try {
            // Use PBKDF2 with SHA-256 for password hashing
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public Category getRootCategory() {
        return adapter.getRootCategory();
    }

    public void addPasswordToCategory(String categoryPath, Passwords password) {
        adapter.addPasswordToCategory(categoryPath, password);
    }

    public void displayCategoryHierarchy() {
        Category root = adapter.getRootCategory();
        root.show();
    }

    public PasswordManagerCaretaker getCaretaker() {
        return caretaker;
    }
}
