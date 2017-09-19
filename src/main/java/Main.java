import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("WeakerAccess")
public class Main {
    public static void main (String[] args){
        System.out.println(Main.getOlaMundo());
    }

    @NotNull
    @Contract(pure = true)
    protected static String getOlaMundo(){
        return "Olá Mundo";
    }
}
