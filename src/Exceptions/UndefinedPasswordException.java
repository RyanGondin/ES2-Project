package Exceptions;

/**
 * Custom exception to indicate that a password is undefined or not found.
 */
public class UndefinedPasswordException extends Exception {
    /**
     * Constructor that takes an error message and prints it to the console.
     * @param msg the error message to be displayed
     */
    public UndefinedPasswordException(String msg) {
        System.out.println(msg);

    }
}