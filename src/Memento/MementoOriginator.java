package Memento;

import Composite.Category;
import Interfaces.Passwords;

import java.util.LinkedHashMap;
import java.util.List;

public class MementoOriginator {

    private LinkedHashMap<String, Passwords> passwords;
    private Category rootCategory;
    private String lastAccessedPasswordId;
    private PasswordManagerCaretaker caretaker = new PasswordManagerCaretaker(10);

    public MementoOriginator() {
        this.passwords = new LinkedHashMap<>();
        this.rootCategory = new Category("root");
    }

    // Restore state from a memento
    public void restoreFromMemento(PasswordManagerMemento memento) {
        if (memento != null) {
            restoreState(
                    memento.getSavedPasswords(),
                    memento.getSavedRootCategory()
            );
            this.lastAccessedPasswordId = memento.getLastAccessedPasswordId();
        }
    }

    private PasswordManagerMemento saveState() {
        PasswordManagerMemento memento = createMemento();
        caretaker.addMemento(memento);
        return memento;
    }

    private void restoreState(LinkedHashMap<String, Passwords> savedPasswords, Category savedRootCategory) {
        this.passwords = new LinkedHashMap<>(savedPasswords);
        this.rootCategory = savedRootCategory;
    }

    private PasswordManagerMemento createMemento() {
        return new PasswordManagerMemento(
                this.passwords,
                this.rootCategory,
                this.lastAccessedPasswordId
        );
    }
}