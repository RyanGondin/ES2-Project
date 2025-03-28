package Composite;
import java.util.ArrayList;
import java.util.List;
import Interfaces.PasswordCategory;

public class Category implements PasswordCategory {
    private String name;
    private List<PasswordCategory> children = new ArrayList<>();

    public Category(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public void show() {
        System.out.println("Category: " + name);
        for (PasswordCategory child : children) {
            child.show();
        }
    }

    @Override
    public void add(PasswordCategory category) {
        children.add(category);
    }

    @Override
    public void remove(PasswordCategory category) {
        children.remove(category);
    }

    @Override
    public List<PasswordCategory> getChildren() {
        return children;
    }
}
