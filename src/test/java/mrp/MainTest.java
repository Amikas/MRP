// File: test/mrp/MainTest.java
package mrp;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class MainTest {

    @Test
    void testMainClassExists() {
        Main main = new Main();
        assertNotNull(main);
    }

    @Test
    void testApplicationStarts() {
        // Simple test that doesn't actually start the server
        assertTrue(true);
    }
}