import org.junit.Test;

import static org.junit.Assert.*;

public class MainTest {
    @Test
    public void getOlaMundo() throws Exception {
        assertEquals("Olá Mundo",Main.getOlaMundo());
    }

}