package {{packageName}};

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class {{className}}Test {
    @Test
    void testGreet() {
        {{className}} app = new {{className}}();
        assertEquals("Hello, Test!", app.greet("Test"));
    }
}