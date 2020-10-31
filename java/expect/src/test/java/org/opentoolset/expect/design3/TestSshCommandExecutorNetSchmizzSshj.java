package org.opentoolset.expect.design3;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class TestSshCommandExecutorNetSchmizzSshj extends AbstractTestCommandExecutor {

	@Test
	public void testExecutingCommands() throws Exception {
		SshCommandExecutorNetSchmizzSshj.SessionCreator sessionCreator = buildSessionCreator();
		testExecutingCommands(sessionCreator);
	}

	@Test
	public void testManagingJavaKeystore() throws Exception {
		SshCommandExecutorNetSchmizzSshj.SessionCreator sessionCreator = buildSessionCreator();
		sessionCreator.withDefaultTimeout(1, TimeUnit.SECONDS);
		testManagingJavaKeystore(sessionCreator);
	}

	// ---

	private SshCommandExecutorNetSchmizzSshj.SessionCreator buildSessionCreator() {
		SshCommandExecutorNetSchmizzSshj.SessionCreator sessionCreator = new SshCommandExecutorNetSchmizzSshj.SessionCreator();
		sessionCreator.withHostname("localhost");
		String username = System.getProperty("user.name");
		sessionCreator.withUsername(username);
		sessionCreator.withEchoOutput(System.err);
		sessionCreator.withEchoInputs(System.out);
		return sessionCreator;
	}
}
