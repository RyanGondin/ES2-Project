package Memento;

import Composite.Category;
import Interfaces.Passwords;

import java.util.LinkedHashMap;
import java.util.List;
import java.time.LocalDateTime;

public class MementoOriginator {

    private LinkedHashMap<String, Passwords> passwords;
    private Category rootCategory;
    private String lastAccessedPasswordId;
    private PasswordManagerCaretaker caretaker = new PasswordManagerCaretaker(10);

    public MementoOriginator() {
        this.passwords = new LinkedHashMap<>();
        this.rootCategory = new Category("root");
    }

    // Save the current state to a memento
    public void saveState() {
        PasswordManagerMemento memento = createMemento();
        caretaker.addMemento(memento);
    }

    // Restore state from a memento
    public void restoreFromMemento(PasswordManagerMemento memento) {
        if (memento != null) {
            this.passwords = new LinkedHashMap<>(memento.getSavedPasswords());
            this.rootCategory = memento.getSavedRootCategory();
            this.lastAccessedPasswordId = memento.getLastAccessedPasswordId();
        }
    }

    // Create a memento from the current state
    private PasswordManagerMemento createMemento() {
        return new PasswordManagerMemento(
                this.passwords,
                this.rootCategory,
                this.lastAccessedPasswordId
        );
    }

    public PasswordManagerCaretaker getCaretaker() {
        return caretaker;
    }

    public List<LocalDateTime> getMementoTimestamps() {
        return caretaker.getMementoTimestamps();
    }

    public PasswordManagerMemento getMemento(int index) {
        return caretaker.getMemento(index);
    }
}