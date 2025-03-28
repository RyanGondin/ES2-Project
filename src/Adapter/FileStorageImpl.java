package Adapter;
import java.io.*;

public class FileStorageImpl implements FileStorage {
    private final String filePath;

    public FileStorageImpl(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void writeData(String data) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(data);
        }
    }

    @Override
    public String readData() throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    @Override
    public boolean exists() {
        return new File(filePath).exists();
    }

    @Override
    public void create() throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    @Override
    public void delete() throws IOException {
        File file = new File(filePath);
        if (!file.delete() && file.exists()) {
            throw new IOException("Failed to delete file: " + filePath);
        }
    }
}