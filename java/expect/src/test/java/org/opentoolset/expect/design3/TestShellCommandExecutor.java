package org.opentoolset.expect.design3;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class TestShellCommandExecutor extends AbstractTestCommandExecutor {

	@Test
	public void testExecutingSingleCommand() throws Exception {
		ShellCommandExecutor.SessionCreator sessionCreator = new ShellCommandExecutor.SessionCreator();
		testExecutingSingleCommand(sessionCreator);
	}

	@Test
	public void testManagingJavaKeystore() throws Exception {
		ShellCommandExecutor.SessionCreator sessionCreator = new ShellCommandExecutor.SessionCreator();
		sessionCreator.withShell("/bin/sh");
		sessionCreator.withDefaultTimeout(1, TimeUnit.SECONDS);
		testManagingJavaKeystore(sessionCreator);
	}
}
