package net.sf.sahi.command;

import junit.framework.TestCase;
import net.sf.sahi.command.MockResponder;

public class CustomResponseManagerTest extends TestCase {

    public void testGetCommand() {
        MockResponder mockResponder = new MockResponder();
        mockResponder.add(".*sahi[.]co[.]in.*", "net.sf.sahi.Test_test");
        String command = mockResponder.getCommand("http://www.sahi.co.in");
        assertEquals("net.sf.sahi.Test_test", command);
    }

}
