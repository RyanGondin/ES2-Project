package ReusablePool;
import Exceptions.ObjectNotFoundException;
import Exceptions.PoolExhaustedException;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * DatabaseConnectionReusablePool class represents a pool of reusable database connections.
 */
public class ReusablePool {

    private static ReusablePool instance = null;
    private ArrayList<HttpURLConnection> pool = new ArrayList<HttpURLConnection>();
    private int maxSize = 10;

    /**
     * Private constructor to prevent instantiation from outside the class.
     */
    private ReusablePool() {}


    /**
     * Return the singleton instance. If the instance does not exist, it creates one.
     * @return the singleton instance
     */
    public static ReusablePool getInstance(){
        if (instance == null) instance = new ReusablePool();
        return instance;
    }

    /**
     * Method to acquire a database connection object from the pool for a given URL
     * @param url the URL to connect to databse
     * @return database connection object
     * @throws IOException if an I/O error occurs
     * @throws PoolExhaustedException if the pool is full
     */
    public HttpURLConnection acquire(URL url) throws IOException, PoolExhaustedException {
        HttpURLConnection con = null;
        if (this.pool.size() < this.maxSize) {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            this.pool.add(con);
        } else throw new PoolExhaustedException();
        return con;
    }

    /**
     * Method to release a database connection from the pool
     * @param conn the database connection object to release
     * @throws ObjectNotFoundException if the connection object is not found in the pool
     */
    public void release(HttpURLConnection conn) throws ObjectNotFoundException {
        int index = this.pool.indexOf(conn);
        if (index != -1) {
            this.pool.remove(index);
        } else throw new ObjectNotFoundException();
    }

    /**
     * Method to reset the pool
     */
    public void resetPool() {
        instance = null;
    }

    /**
     * Method to set the maximum pool size
     * @param size the maximum pool size
     */
    public void setMaxPoolSize(int size) {
        this.maxSize = size;
    }

}