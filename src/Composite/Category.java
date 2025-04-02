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

    public void saveToDb() throws IOException, PoolExhaustedException {
        HttpURLConnection connection = dbConnection.acquire(dbUrl);
        // do nothing
    }
}
