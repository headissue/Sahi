package net.sf.sahi.session;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Sahi - Web Automation and Test Tool
 * <p/>
 * Copyright  2006  V Narayan Raman
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class SessionTest {

  Session session = new Session("");

  @Test
  public void testRemoveVariables() {
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

  @Test
  public void testSessionState() {
    session.setIsRecording(true);
    assertTrue(session.isRecording());
    session.setIsRecording(false);
    assertFalse(session.isRecording());
  }

  @Test
  public void testRemoveInactiveDoesNotRemoveRecordingSessions() throws Exception {
    session.setIsPlaying(true);
    assertEquals(Session.playbackInactiveTimeout, session.getInactiveTimeout(), 0);
    session.setIsPlaying(false);
    assertEquals(Session.recorderInactiveTimeout, session.getInactiveTimeout(), 0);

  }
}
