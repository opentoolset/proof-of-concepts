package org.opentoolset.poc.expect;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.opentoolset.poc.expect.CommandExecutor.SessionCreator;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import net.sf.expectit.filter.Filters;
import net.sf.expectit.matcher.Matchers;

public class TestCommandExecutor extends AbstractTestCommandExecutor {

	@Test
	public void testExecutingCommandsOnLocalShell() throws Exception {
		Process process = createProcess();
		CommandExecutor.SessionCreator sessionCreator = buildSessionCreatorForLocalShell(process);
		testExecutingCommands(sessionCreator);
		closeProcess(process);
	}

	@Test
	public void testManagingJavaKeystoreOnLocalShell() throws Exception {
		Process process = createProcess();
		CommandExecutor.SessionCreator sessionCreator = buildSessionCreatorForLocalShell(process);
		testManagingJavaKeystore(sessionCreator);
		closeProcess(process);
	}

	@Test
	public void testManagingJavaKeystoreOnLocalShellDeclaratively() throws Exception {
		Process process = createProcess();
		CommandExecutor.SessionCreator sessionCreator = buildSessionCreatorForLocalShell(process);
		testManagingJavaKeystoreDeclaratively(sessionCreator);
		closeProcess(process);
	}

	@Test
	public void testExecutingCommandsInSSHSession() throws Exception {
		Session sshSession = openSSHSession();
		Channel sshChannel = openSSHChannel(sshSession);
		CommandExecutor.SessionCreator sessionCreator = buildSessionCreatorForSSH(sshChannel);
		testExecutingCommands(sessionCreator);
		sshChannel.disconnect();
		sshSession.disconnect();
	}

	@Test
	public void testManagingJavaKeystoreInSSHSession() throws Exception {
		Session sshSession = openSSHSession();
		Channel sshChannel = openSSHChannel(sshSession);
		CommandExecutor.SessionCreator sessionCreator = buildSessionCreatorForSSH(sshChannel);
		testManagingJavaKeystore(sessionCreator);
		sshChannel.disconnect();
		sshSession.disconnect();
	}

	@Test
	public void testManagingJavaKeystoreInSSHSessionDeclaratively() throws Exception {
		Session sshSession = openSSHSession();
		Channel sshChannel = openSSHChannel(sshSession);
		CommandExecutor.SessionCreator sessionCreator = buildSessionCreatorForSSH(sshChannel);
		testManagingJavaKeystoreDeclaratively(sessionCreator);
		sshChannel.disconnect();
		sshSession.disconnect();
	}

	// ---

	private CommandExecutor.SessionCreator buildSessionCreatorForLocalShell(Process process) {
		CommandExecutor.SessionCreator sessionCreator = new CommandExecutor.SessionCreator();
		sessionCreator.withOutput(process.getOutputStream());
		sessionCreator.withInputs(process.getInputStream(), process.getErrorStream());
		sessionCreator.withDefaultMatcherProvider(() -> Matchers.regexp("\n$"));
		sessionCreator.withDefaultTimeout(1, TimeUnit.SECONDS);
		sessionCreator.withEchoOutput(System.err);
		sessionCreator.withEchoInputs(System.out);
		return sessionCreator;
	}

	private SessionCreator buildSessionCreatorForSSH(Channel channel) throws IOException {
		CommandExecutor.SessionCreator sessionCreator = new CommandExecutor.SessionCreator();
		sessionCreator.withOutput(channel.getOutputStream());
		sessionCreator.withInputs(channel.getInputStream(), channel.getExtInputStream());
		sessionCreator.withInputFilters(Filters.removeColors(), Filters.removeNonPrintable());
		sessionCreator.withDefaultMatcherProvider(() -> Matchers.regexp("\\$"));
		sessionCreator.withDefaultTimeout(1, TimeUnit.SECONDS);
		sessionCreator.withEchoOutput(System.err);
		sessionCreator.withEchoInputs(System.out);
		return sessionCreator;
	}

	private Process createProcess() throws IOException {
		Process process = new ProcessBuilder("/bin/sh").start();
		return process;
	}

	private void closeProcess(Process process) {
		try {
			process.waitFor(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		}
		process.destroy();
	}

	private Session openSSHSession() throws JSchException {
		JSch jSch = new JSch();
		jSch.addIdentity(System.getProperty("user.home") + "/.ssh/id_rsa");
		Session sshSession = jSch.getSession(System.getProperty("user.name"), "localhost");

		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		sshSession.setConfig(config);
		sshSession.connect();
		return sshSession;
	}

	private Channel openSSHChannel(Session sshSession) throws JSchException {
		Channel channel = sshSession.openChannel("shell");
		channel.connect();
		return channel;
	}
}
