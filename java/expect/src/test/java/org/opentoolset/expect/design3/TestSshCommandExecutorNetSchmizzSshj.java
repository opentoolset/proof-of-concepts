package org.opentoolset.expect.design3;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class TestSshCommandExecutorNetSchmizzSshj extends AbstractTestCommandExecutor {

	@Test
	public void testExecutingSingleCommand() throws Exception {
		SshCommandExecutorWithNetSchmizzSshj.SessionCreator sessionCreator = buildSessionCreator();
		testExecutingSingleCommand(sessionCreator);
	}

	@Test
	public void testManagingJavaKeystore() throws Exception {
		SshCommandExecutorWithNetSchmizzSshj.SessionCreator sessionCreator = buildSessionCreator();
		sessionCreator.withDefaultTimeout(1, TimeUnit.SECONDS);
		testManagingJavaKeystore(sessionCreator);
	}

	// ---

	private SshCommandExecutorWithNetSchmizzSshj.SessionCreator buildSessionCreator() {
		SshCommandExecutorWithNetSchmizzSshj.SessionCreator sessionCreator = new SshCommandExecutorWithNetSchmizzSshj.SessionCreator();
		sessionCreator.withHostname("localhost");
		String username = System.getProperty("user.name");
		sessionCreator.withUsername(username);
		return sessionCreator;
	}
}
