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

    public void show() {
        show(0);
    }

    private void show(int indent) {

    }

    public void add(PasswordCategory category) {
        children.add(category);
    }


    public void remove(PasswordCategory category) {
        children.remove(category);
    }

    public List<PasswordCategory> getChildren() {
        return children;
    }

    public void addPasswordId(String id) {
        passwordIds.add(id);
    }

    public List<String> getPasswordIds() {
        return passwordIds;
    }

    public boolean containsPasswordId(String id) {
        return passwordIds.contains(id);
    }

    public void saveToDb() throws IOException, PoolExhaustedException {
        HttpURLConnection connection = dbConnection.acquire(dbUrl);
    }

    public void someMethodWithConnection() {
    }
}
