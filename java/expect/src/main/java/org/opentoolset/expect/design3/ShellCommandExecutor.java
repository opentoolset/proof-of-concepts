package org.opentoolset.expect.design3;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ShellCommandExecutor {

	public static class SessionCreator extends CommandExecutor.SessionCreator {

		private Session session = buildSession();

		public SessionCreator withShell(String shell) {
			this.session.shell = shell;
			return this;
		}

		@Override
		public Session create() throws IOException {
			Process process = this.session.startShellProcess();
			withOutput(process.getOutputStream());
			withInputs(process.getInputStream(), process.getErrorStream());

			this.session.create();
			return this.session;
		}

		@Override
		protected Session buildSession() {
			return new Session();
		}
	}

	public static class Session extends CommandExecutor.Session {

		private String shell = "/bin/sh";
		private Process process;

		@Override
		public void close() throws IOException {
			super.close();
			try {
				this.process.waitFor(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
			}
			this.process.destroy();
		}

		private Process startShellProcess() throws IOException {
			this.process = new ProcessBuilder(this.shell).start();
			return this.process;
		}
	}
}
