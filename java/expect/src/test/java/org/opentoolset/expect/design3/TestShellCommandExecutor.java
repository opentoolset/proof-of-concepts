package org.opentoolset.expect.design3;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class TestShellCommandExecutor extends AbstractTestCommandExecutor {

	@Test
	public void testExecutingSingleCommand() throws Exception {
		ShellCommandExecutor.SessionBuilder sessionBuilder = new ShellCommandExecutor.SessionBuilder();
		testExecutingSingleCommand(sessionBuilder);
	}

	@Test
	public void testManagingJavaKeystore() throws Exception {
		ShellCommandExecutor.SessionBuilder sessionBuilder = new ShellCommandExecutor.SessionBuilder();
		sessionBuilder.withShell("/bin/sh");
		sessionBuilder.withDefaultTimeout(1, TimeUnit.SECONDS);
		testManagingJavaKeystore(sessionBuilder);
	}
}
