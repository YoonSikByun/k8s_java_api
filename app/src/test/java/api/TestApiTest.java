/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package api;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestApiTest {
    @Test public void appHasAGreeting() {
        TestApi classUnderTest = new TestApi();
        assertNotNull("app should have a greeting", classUnderTest.getGreeting());
    }
}
