package Exceptions;

public class UndefinedPasswordException extends Exception {
    public UndefinedPasswordException(String msg) {
        System.out.println(msg);

    }
}
