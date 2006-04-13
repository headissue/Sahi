package net.sf.sahi.command;

import junit.framework.TestCase;

import net.sf.sahi.request.HttpRequest;

public class CommandExecuterTest extends TestCase {
	static boolean called = false;

	public void testMethodCalled() throws Exception {
		final HttpRequest httpRequest = null;
		new CommandExecuter("net.sf.sahi.command.TestClass_act", httpRequest).execute();
		assertTrue(called);
	}

	public void testMethodCalledWithoutClassFQN() throws Exception {
		final HttpRequest httpRequest = null;
		new CommandExecuter("TestClass_act", httpRequest).execute();
		assertTrue(called);
	}

	public void tearDown() {
		called = false;
	}

}
