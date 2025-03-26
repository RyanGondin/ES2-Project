package Interfaces;
import java.util.List;

public interface PasswordCategory {
    void show();
    void add(PasswordCategory category);
    void remove(PasswordCategory category);
    List<PasswordCategory> getChildren();
}
