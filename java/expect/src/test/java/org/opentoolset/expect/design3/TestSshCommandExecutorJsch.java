package org.opentoolset.expect.design3;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class TestSshCommandExecutorJsch extends AbstractTestCommandExecutor {

	@Test
	public void testExecutingSingleCommand() throws Exception {
		SshCommandExecutorWithJsch.SessionCreator sessionCreator = buildSessionCreator();
		testExecutingSingleCommand(sessionCreator);
	}

	@Test
	public void testManagingJavaKeystore() throws Exception {
		SshCommandExecutorWithJsch.SessionCreator sessionCreator = buildSessionCreator();
		testManagingJavaKeystore(sessionCreator);
	}

	// ---

	private SshCommandExecutorWithJsch.SessionCreator buildSessionCreator() {
		SshCommandExecutorWithJsch.SessionCreator sessionCreator = new SshCommandExecutorWithJsch.SessionCreator();
		sessionCreator.withHostname("localhost");
		String username = System.getProperty("user.name");
		sessionCreator.withUsername(username);
		sessionCreator.withDefaultTimeout(1, TimeUnit.SECONDS);
		return sessionCreator;
	}
}
