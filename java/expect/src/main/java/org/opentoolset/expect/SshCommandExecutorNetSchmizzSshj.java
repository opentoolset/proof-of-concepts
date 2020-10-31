package org.opentoolset.expect;

import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import org.opentoolset.expect.SshCommandExecutorNetSchmizzSshj.Session.CompositeUserAuthException;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session.Shell;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;
import net.sf.expectit.filter.Filters;

/**
 * This has not been tested yet
 */
public class SshCommandExecutorNetSchmizzSshj {

	public static class SessionCreator extends CommandExecutor.SessionCreator {

		private Session session = buildSession();

		public SessionCreator withHostname(String hostname) {
			this.session.hostname = hostname;
			return this;
		}

		public SessionCreator withUsername(String username) {
			this.session.username = username;
			return this;
		}

		public SessionCreator withPassword(String password) {
			this.session.password = password;
			return this;
		}

		public SessionCreator withPrivateKeys(String... keyFilePaths) {
			this.session.keyFilePaths = keyFilePaths;
			return this;
		}

		@Override
		public Session create() throws IOException, CompositeUserAuthException {
			Shell sshShell = this.session.startShell();
			withOutput(sshShell.getOutputStream());
			withInputs(sshShell.getInputStream(), sshShell.getErrorStream());
			withInputFilters(Filters.removeColors(), Filters.removeNonPrintable());

			this.session.create();
			return this.session;
		}

		@Override
		protected Session buildSession() {
			return new Session();
		}
	}

	// ---

	public static class Session extends CommandExecutor.Session {

		private SSHClient sshClient;
		private net.schmizz.sshj.connection.channel.direct.Session sshSession;
		private Shell sshShell;

		private String hostname;
		private String username;
		private String password;
		private String[] keyFilePaths;

		@Override
		public void close() throws IOException {
			super.close();
			this.sshShell.close();
			this.sshSession.close();
			this.sshClient.close();
		}

		private Shell startShell() throws IOException, CompositeUserAuthException {
			this.sshClient = new SSHClient();
			this.sshClient.addHostKeyVerifier((hostname, port, publicKey) -> true);
			this.sshClient.connect(this.hostname);
			tryAuth();
			this.sshSession = this.sshClient.startSession();
			this.sshSession.allocateDefaultPTY();

			this.sshShell = this.sshSession.startShell();
			return this.sshShell;
		}

		private void tryAuth() throws TransportException, CompositeUserAuthException {
			List<UserAuthException> userAuthExceptions = new ArrayList<>();
			if (this.username != null) {
				try {
					tryPublicKeyAuth();
				} catch (UserAuthException e) {
					userAuthExceptions.add(e);
				}

				if (!this.sshClient.isAuthenticated()) {
					try {
						tryPasswordAuth();
					} catch (UserAuthException e) {
						userAuthExceptions.add(e);
					}
				}
			}

			if (!userAuthExceptions.isEmpty()) {
				throw new CompositeUserAuthException(userAuthExceptions);
			}
		}

		private void tryPublicKeyAuth() throws UserAuthException, TransportException {
			if (this.keyFilePaths != null) {
				this.sshClient.authPublickey(this.username, this.keyFilePaths);
			} else {
				this.sshClient.authPublickey(this.username);
			}
		}

		private void tryPasswordAuth() throws UserAuthException, TransportException {
			if (this.password != null) {
				this.sshClient.authPassword(this.username, this.password);
			}
		}

		// ---

		public static class CompositeUserAuthException extends Exception {

			@Serial
			private static final long serialVersionUID = 1L;

			private List<UserAuthException> causes;

			public CompositeUserAuthException(List<UserAuthException> causes) {
				this.causes = causes;
			}

			public List<UserAuthException> getCauses() {
				return causes;
			}
		}
	}
}
