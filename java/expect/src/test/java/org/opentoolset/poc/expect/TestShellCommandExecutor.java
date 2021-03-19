package org.opentoolset.poc.expect;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.opentoolset.poc.expect.ShellCommandExecutor;

public class TestShellCommandExecutor extends AbstractTestCommandExecutor {

	@Test
	public void testExecutingCommands() throws Exception {
		ShellCommandExecutor.SessionCreator sessionCreator = buildSessionCreator();
		testExecutingCommands(sessionCreator);
	}

	@Test
	public void testManagingJavaKeystore() throws Exception {
		ShellCommandExecutor.SessionCreator sessionCreator = buildSessionCreator();
		testManagingJavaKeystore(sessionCreator);
	}

	@Test
	public void testManagingJavaKeystoreDeclaratively() throws Exception {
		ShellCommandExecutor.SessionCreator sessionCreator = buildSessionCreator();
		testManagingJavaKeystoreDeclaratively(sessionCreator);
	}

	private ShellCommandExecutor.SessionCreator buildSessionCreator() {
		ShellCommandExecutor.SessionCreator sessionCreator = new ShellCommandExecutor.SessionCreator();
		sessionCreator.withShell("/bin/sh");
		sessionCreator.withDefaultTimeout(1, TimeUnit.SECONDS);
		sessionCreator.withEchoOutput(System.err);
		sessionCreator.withEchoInputs(System.out);
		return sessionCreator;
	}
}
