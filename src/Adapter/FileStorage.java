package Adapter;

import java.io.IOException;

public interface FileStorage {
    void writeData(String data) throws IOException;
    String readData() throws IOException;
    boolean exists();
    void create() throws IOException;
    void delete() throws IOException;
}