package org.opentoolset.poc.expect;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class TestSshCommandExecutorJsch extends AbstractTestCommandExecutor {

	@Test
	public void testExecutingCommands() throws Exception {
		SshCommandExecutorJsch.SessionCreator sessionCreator = buildSessionCreator();
		testExecutingCommands(sessionCreator);
	}

	@Test
	public void testManagingJavaKeystore() throws Exception {
		SshCommandExecutorJsch.SessionCreator sessionCreator = buildSessionCreator();
		testManagingJavaKeystore(sessionCreator);
	}

	@Test
	public void testManagingJavaKeystoreDeclaratively() throws Exception {
		SshCommandExecutorJsch.SessionCreator sessionCreator = buildSessionCreator();
		testManagingJavaKeystoreDeclaratively(sessionCreator);
	}

	// ---

	private SshCommandExecutorJsch.SessionCreator buildSessionCreator() {
		SshCommandExecutorJsch.SessionCreator sessionCreator = new SshCommandExecutorJsch.SessionCreator();
		sessionCreator.withHostname("localhost");
		String username = System.getProperty("user.name");
		sessionCreator.withUsername(username);
		sessionCreator.withDefaultTimeout(1, TimeUnit.SECONDS);
		sessionCreator.withEchoOutput(System.err);
		sessionCreator.withEchoInputs(System.out);
		sessionCreator.withSSHProperty("StrictHostKeyChecking", "no");
		return sessionCreator;
	}
}
