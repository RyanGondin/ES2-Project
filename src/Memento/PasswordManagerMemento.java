package Memento;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;

import Interfaces.Passwords;
import Composite.Category;
import Interfaces.PasswordCategory;
import Factory.FactoryPassword;

/**
 * Represents a snapshot of the password manager state
 * that can be restored later.
 */
public class PasswordManagerMemento implements Serializable {
    private final LinkedHashMap<String, Passwords> passwordsState;
    private final Category rootCategoryState;
    private final String lastAccessedPasswordId;
    private final LocalDateTime timestamp;
    
    public PasswordManagerMemento(
            LinkedHashMap<String, Passwords> passwords,
            Category rootCategory, 
            String lastAccessedPasswordId) {
        
        // Create deep copies to ensure immutability
        this.passwordsState = new LinkedHashMap<>(passwords);
        this.rootCategoryState = deepCopyCategory(rootCategory);
        this.lastAccessedPasswordId = lastAccessedPasswordId;
        this.timestamp = LocalDateTime.now();
    }
    
    public LinkedHashMap<String, Passwords> getSavedPasswords() {
        return new LinkedHashMap<>(passwordsState);
    }
        
    public Category getSavedRootCategory() {
        return deepCopyCategory(rootCategoryState);
    }
    
    public String getLastAccessedPasswordId() {
        return lastAccessedPasswordId;
    }
    
    // Public method for displaying metadata without exposing state
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    
    private Category deepCopyCategory(Category original) {
        if (original == null) return null;
        
        Category copy = new Category(original.getName());
        
        // Copy all children
        for (PasswordCategory child : original.getChildren()) {
            if (child instanceof Category) {
                copy.add(deepCopyCategory((Category) child));
            } else if (child instanceof Passwords) {
                // For passwords, we need to create a new instance
                Passwords originalPassword = (Passwords) child;
                try {
                    Passwords passwordCopy = FactoryPassword.makePassword(originalPassword.getType());
                    passwordCopy.setName(originalPassword.getName());
                    passwordCopy.setUsername(originalPassword.getUsername());
                    passwordCopy.setPassword(originalPassword.getPassword());
                    copy.add((PasswordCategory) passwordCopy);
                } catch (Exception e) {
                    System.err.println("Error copying password: " + e.getMessage());
                }
            }
        }
        
        return copy;
    }

    public PasswordManagerMemento createMemento() {
        return new PasswordManagerMemento(
                getSavedPasswords(),
                getSavedRootCategory(),
                lastAccessedPasswordId
        );
    }
}