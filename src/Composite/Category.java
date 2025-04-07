package Composite;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import Exceptions.PoolExhaustedException;
import Interfaces.PasswordCategory;
import ReusablePool.ReusablePool;

public class Category implements PasswordCategory {
    private String name;
    private List<PasswordCategory> children = new ArrayList<>();
    private List<String> passwordIds = new ArrayList<>(); // Add this field
    private ReusablePool dbConnection = ReusablePool.getInstance();
    private URL dbUrl;
    {
        try {
            dbUrl = new URL("http://localhost:8080/db");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public Category(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public void show() {
        show(0);
    }

    private void show(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }
        sb.append("- ").append(name);
        if (!passwordIds.isEmpty()) {
            sb.append(" (").append(passwordIds.size()).append(" passwords)");
        }
        System.out.println(sb.toString());

        for (PasswordCategory child : children) {
            if (child instanceof Category) {
                ((Category) child).show(indent + 1);
            } else {
                child.show();
            }
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

    public void addPasswordId(String id) {
        passwordIds.add(id);
    }

    public List<String> getPasswordIds() {
        return passwordIds;
    }

    public void saveToDb() throws IOException, PoolExhaustedException {
        HttpURLConnection connection = dbConnection.acquire(dbUrl);
        // do nothing
    }

    public void someMethodWithConnection() {
        // If you don't need the connection variable, remove it
        // Or use it properly
    }
}
