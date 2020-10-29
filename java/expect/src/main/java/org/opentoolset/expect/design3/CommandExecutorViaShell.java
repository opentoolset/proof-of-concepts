package org.opentoolset.expect.design3;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CommandExecutorViaShell {

	public static class SessionBuilder extends CommandExecutor.SessionBuilder {

		private Session session = new Session();

		public SessionBuilder withShell(String shell) {
			this.session.shell = shell;
			return this;
		}

		@Override
		public Session create() throws IOException {
			this.session.process = new ProcessBuilder(this.session.shell).start();
			withOutputStream(this.session.process.getOutputStream());
			withInputStream(this.session.process.getInputStream());
			withErrorStream(this.session.process.getErrorStream());
			prepare(session);
			return this.session;
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
	}
}
