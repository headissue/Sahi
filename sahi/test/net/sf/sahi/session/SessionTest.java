package net.sf.sahi.session;

import junit.framework.TestCase;

public class SessionTest extends TestCase {
    Session session = new Session("");
    public void testRemoveVariables(){
        session.setVariable("condn1", "1");
        session.setVariable("condn2", "2");
        session.setVariable("condn3", "3");

        assertEquals("1", session.getVariable("condn1"));
        assertEquals("2", session.getVariable("condn2"));
        assertEquals("3", session.getVariable("condn3"));

        session.removeVariables("condn.*");

        assertEquals(null, session.getVariable("condn1"));
        assertEquals(null, session.getVariable("condn2"));
        assertEquals(null, session.getVariable("condn3"));

    }
}
