package Memento;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

/**
 * Responsible for storing and managing mementos
 * without knowing their internal structure.
 */
public class PasswordManagerCaretaker {
    private final List<PasswordManagerMemento> mementos = new ArrayList<>();
    private final int maxHistorySize;

    public PasswordManagerCaretaker(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
    }

    public void addMemento(PasswordManagerMemento memento) {
        mementos.add(memento);
        if (mementos.size() > maxHistorySize) {
            mementos.remove(0);
        }
    }

    public PasswordManagerMemento getMemento(int index) {
        if (index >= 0 && index < mementos.size()) {
            return mementos.get(index);
        }
        return null;
    }

    public List<LocalDateTime> getMementoTimestamps() {
        List<LocalDateTime> timestamps = new ArrayList<>();
        for (PasswordManagerMemento memento : mementos) {
            timestamps.add(memento.getTimestamp());
        }
        return timestamps;
    }
}