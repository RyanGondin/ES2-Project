package Decorator;

import Adapter.StorageAPI;
import java.util.Scanner;

public class MFAStorageDecorator extends StorageDecorator {
    public MFAStorageDecorator(StorageAPI storageAPI) {
        super(storageAPI);
    }

    @Override
    public String setStorage(Interfaces.Passwords password) {
        if (!autenticarMFA()) {
            System.out.println("Autenticação falhou. Operação cancelada.");
            return null;
        }
        return super.setStorage(password);
    }

    private boolean autenticarMFA() {
        // Usando try-with-resources para garantir que o scanner seja fechado corretamente
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Digite o código de autenticação multifator (ex: 1234): ");
            String code = scanner.nextLine();
            return code.equals("1234"); // Simulação de MFA
        } catch (Exception e) {
            System.out.println("Erro ao ler a entrada: " + e.getMessage());
            return false;
        }
    }
}
