import Interfaces.Passwords;
import Exceptions.UndefinedPasswordException;
import Interfaces.PasswordType;
import Interfaces.StorageImplementation;
import Interfaces.StorageManager;
import Interfaces.PasswordCategory;

public class Main {
    public static void main(String[] args) {
        try {
            // Criando senhas com o Factory
            Passwords strongPassword = FactoryPassword.makePassword(PasswordType.STRONG);
            strongPassword.setPassword("StrongPass123");
            strongPassword.show(); // Expected output: Password: StrongPass123

            Passwords standartPassword = FactoryPassword.makePassword(PasswordType.STANDART);
            standartPassword.setPassword("StandartPass123");
            standartPassword.show(); // Expected output: Password: StandartPass123

            // Usando o Composite Pattern para categorias de senhas
            Category pessoal = new Category("Pessoal");
            Category produtividade = new Category("Produtividade");

            pessoal.add(produtividade);  // Adicionando subcategoria
            produtividade.add((PasswordCategory) strongPassword);  // Adicionando senha a uma categoria

            pessoal.show(); // Mostra a estrutura de categorias e senhas

            // Usando StorageAPI para gerenciamento de armazenamento
            StorageImplementation localStorage = new StorageAPI();
            StorageManager storageManager = new StorageRequest(localStorage);

            // Armazenando e recuperando dados
            String storageId = storageManager.setStorage("MyPassword123");
            System.out.println("Stored ID: " + storageId);

            String retrievedData = storageManager.getStorage(storageId);
            System.out.println("Retrieved Data: " + retrievedData);

        } catch (UndefinedPasswordException e) {
            e.printStackTrace();
        }
    }
}
