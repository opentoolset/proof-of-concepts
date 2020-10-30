package org.opentoolset.expect.design3;

import java.io.IOException;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

import net.sf.expectit.filter.Filters;

public class SshCommandExecutorWithJsch {

	public static class SessionCreator extends CommandExecutor.SessionCreator {

		public SessionCreator withHostname(String hostname) {
			getSession().hostname = hostname;
			return this;
		}

		public SessionCreator withUsername(String username) {
			getSession().username = username;
			return this;
		}

		public SessionCreator withPassword(String password) {
			getSession().password = password;
			return this;
		}

		public SessionCreator withIdentity(String identityFile) {
			getSession().identityFile = identityFile;
			return this;
		}

		@Override
		public Session create() throws IOException, JSchException {
			Session session = getSession();
			Channel channel = session.connect();
			withOutput(channel.getOutputStream());
			withInputs(channel.getInputStream(), channel.getExtInputStream());
			withInputFilters(Filters.removeColors(), Filters.removeNonPrintable());

			session.create();
			return getSession();
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

	// ---

	public static class Session extends CommandExecutor.Session {

		private com.jcraft.jsch.Session sshSession;
		private Channel channel;

		private String hostname;
		private String username;
		private String password;
		private String identityFile;

		@Override
		public void close() throws IOException {
			super.close();
			this.channel.disconnect();
			this.sshSession.disconnect();
		}

		private Channel connect() throws JSchException {
			JSch jSch = new JSch();
			if (this.identityFile != null) {
				jSch.addIdentity(this.identityFile);
			} else {
				jSch.addIdentity(System.getProperty("user.home") + "/.ssh/id_rsa");
			}

			this.sshSession = jSch.getSession(this.username, this.hostname);
			this.sshSession.setPassword(this.password);

			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			this.sshSession.setConfig(config);
			this.sshSession.connect();

			this.channel = this.sshSession.openChannel("shell");
			this.channel.connect();
			return this.channel;
		}
	}
}
