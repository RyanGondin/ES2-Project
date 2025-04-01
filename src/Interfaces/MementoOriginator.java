package Interfaces;

import java.util.LinkedHashMap;

import Composite.Category;

/**
 * Interface that only the Originator (StorageAPI) can access
 * to retrieve internal state from a memento
 */
public interface MementoOriginator {
    LinkedHashMap<String, Passwords> getSavedPasswords();
    Category getSavedRootCategory();
    String getLastAccessedPasswordId();
}