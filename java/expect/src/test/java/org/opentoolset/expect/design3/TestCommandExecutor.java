package org.opentoolset.expect.design3;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class TestCommandExecutor extends AbstractTestCommandExecutor {

	@Test
	public void testExecutingSingleCommand() throws Exception {
		Process process = createProcess();
		CommandExecutor.SessionCreator sessionCreator = createSessionBuilder(process);
		testExecutingSingleCommand(sessionCreator);
		closeProcess(process);
	}

	@Test
	public void testManagingJavaKeystore() throws Exception {
		Process process = createProcess();
		CommandExecutor.SessionCreator sessionCreator = createSessionBuilder(process);
		sessionCreator.withDefaultTimeout(1, TimeUnit.SECONDS);
		testManagingJavaKeystore(sessionCreator);
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

	private CommandExecutor.SessionCreator createSessionBuilder(Process process) {
		CommandExecutor.SessionCreator sessionBuilder = new CommandExecutor.SessionCreator();
		sessionBuilder.withOutput(process.getOutputStream());
		sessionBuilder.withInputs(process.getInputStream(), process.getErrorStream());
		return sessionBuilder;
	}
}
