package org.opentoolset.expect.design3;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class TestCommandExecutor extends AbstractTestCommandExecutor {

	@Test
	public void testExecutingSingleCommand() throws Exception {
		Process process = createProcess();
		CommandExecutor.SessionBuilder sessionBuilder = createSessionBuilder(process);
		testExecutingSingleCommand(sessionBuilder);
		closeProcess(process);
	}

	@Test
	public void testManagingJavaKeystore() throws Exception {
		Process process = createProcess();
		CommandExecutor.SessionBuilder sessionBuilder = createSessionBuilder(process);
		sessionBuilder.withDefaultTimeout(1, TimeUnit.SECONDS);
		testManagingJavaKeystore(sessionBuilder);
		closeProcess(process);
	}

	// ---

	private void closeProcess(Process process) {
		try {
			process.waitFor(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		}
		process.destroy();
	}

	private Process createProcess() throws IOException {
		Process process = new ProcessBuilder("/bin/sh").start();
		return process;
	}

	private CommandExecutor.SessionBuilder createSessionBuilder(Process process) {
		CommandExecutor.SessionBuilder sessionBuilder = new CommandExecutor.SessionBuilder();
		sessionBuilder.withOutputStream(process.getOutputStream());
		sessionBuilder.withInputStream(process.getInputStream());
		sessionBuilder.withErrorStream(process.getErrorStream());
		return sessionBuilder;
	}
}
