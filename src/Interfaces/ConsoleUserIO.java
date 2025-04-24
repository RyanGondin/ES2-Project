package Interfaces;

import java.util.Scanner;

public class ConsoleUserIO implements UserIO {
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void displayMessage(String message) {
        System.out.println(message);
    }

    @Override
    public String getInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}