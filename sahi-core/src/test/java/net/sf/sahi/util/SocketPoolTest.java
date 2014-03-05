package net.sf.sahi.util;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

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
public class SocketPoolTest {
  private static final long serialVersionUID = 5201601055328888972L;

  private SocketPool pool;

  @Before
  public void setUp() {
    pool = new SocketPool(2);
  }

  /*
   * Test method for 'net.sf.sahi.util.SocketPool.get(String, int)'
   */
  @Test
  public void testGet() throws IOException {
    assertTrue(pool.get().getLocalPort() == 13300);
    pool.returnToPool(13300);
    assertTrue(pool.get().getLocalPort() == 13301);
    pool.returnToPool(13301);
    assertTrue(pool.get().getLocalPort() == 13300);
    pool.returnToPool(13300);
    assertTrue(pool.get().getLocalPort() == 13301);
    pool.returnToPool(13301);
  }

	/*
   * Test method for 'net.sf.sahi.util.SocketPool.release(Socket)'
	 */
//	public void testRelease() {
//
//	}

}
