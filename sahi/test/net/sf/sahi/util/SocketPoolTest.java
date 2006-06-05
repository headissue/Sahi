package net.sf.sahi.util;

import java.io.IOException;

import junit.framework.TestCase;

public class SocketPoolTest extends TestCase {

	private SocketPool pool;

	public void setUp() {
		pool = new SocketPool(2);
	}

	/*
	 * Test method for 'net.sf.sahi.util.SocketPool.get(String, int)'
	 */
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
