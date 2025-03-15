import Exceptions.UndefinedPasswordException;
import Interfaces.Passwords;

public abstract class FactoryPassword {

    public static Passwords makePassword(String type) throws UndefinedPasswordException {
        if(type.equals("Standart")){
            return new Standart();
        } else if (type.equals("Strong")) {
            return new Strong();
        }else {
            throw new UndefinedPasswordException("Invalid Password type: " + type);
        }
    }
}

