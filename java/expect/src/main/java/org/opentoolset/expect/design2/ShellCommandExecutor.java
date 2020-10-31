package org.opentoolset.expect.design2;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import net.sf.expectit.filter.Filters;
import net.sf.expectit.matcher.Matchers;

public class ShellCommandExecutor {

	public static class SessionCreator extends CommandExecutor.SessionCreator {

		public SessionCreator withShell(String shell) {
			getSession().shell = shell;
			return this;
		}

		@Override
		public Session create() throws IOException {
			Session session = getSession();
			Process process = session.startShellProcess();
			withOutput(process.getOutputStream());
			withInputs(process.getInputStream(), process.getErrorStream());
			withInputFilters(Filters.removeColors(), Filters.removeNonPrintable());
			withDefaultMatcherProvider(() -> Matchers.regexp("\n$"));

			session.create();
			return session;
		}

		@Override
		protected Session buildSession() {
			return new Session();
		}

		@Override
		protected Session getSession() {
			return (Session) super.getSession();
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
				e.printStackTrace();
			}
			this.process.destroy();
		}

		private Process startShellProcess() throws IOException {
			this.process = new ProcessBuilder(this.shell).start();
			return this.process;
		}
	}
}
