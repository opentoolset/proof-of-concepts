package org.opentoolset.expect;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

/**
 * This has not been executed successfully yet
 */
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
		sessionCreator.withHostKeyVerifier((hostname, port, publicKey) -> true);
		return sessionCreator;
	}
}
