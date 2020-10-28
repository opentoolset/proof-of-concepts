package org.opentoolset.expect;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.Result;
import net.sf.expectit.matcher.Matcher;
import net.sf.expectit.matcher.Matchers;

public class ShellExecutor {

	public static class SessionBuilder {

		private Session session = new Session();

		public SessionBuilder() {
			this.session = new Session();
		}

		public SessionBuilder withShell(String shell) {
			this.session.shell = shell;
			return this;
		}

		public SessionBuilder withDefaultTimeout(long duration, TimeUnit unit) {
			this.session.duration = duration;
			this.session.unit = unit;
			return this;
		}

		public Session create() throws IOException {
			this.session.create();
			return session;
		}
	}

	public static class Session implements Closeable {

		private String shell = "/bin/sh";
		private long duration = ExpectBuilder.DEFAULT_TIMEOUT_MS;
		private TimeUnit unit = TimeUnit.MILLISECONDS;

		private Process process;
		private Expect expect;

		private Session() {
		}

		private void create() throws IOException {
			this.process = new ProcessBuilder(this.shell).start();

			ExpectBuilder expectBuilder = new ExpectBuilder();
			expectBuilder.withOutput(this.process.getOutputStream());
			expectBuilder.withInputs(this.process.getInputStream(), this.process.getErrorStream());
			expectBuilder.withCombineInputs(true);
			expectBuilder.withTimeout(this.duration, this.unit);

			this.expect = expectBuilder.build();
		}

		public Result sendLine(String command) throws IOException {
			Map<Matcher<?>, Result> resultMap = sendLine(command, this.duration, this.unit);
			return resultMap.values().iterator().next();
		}

		public Result sendLine(String command, Matcher<?> matcher) throws IOException {
			Map<Matcher<?>, Result> resultMap = sendLine(command, this.duration, this.unit, matcher);
			return resultMap.values().iterator().next();
		}

		public Map<Matcher<?>, Result> sendLine(String command, Matcher<?>... matchers) throws IOException {
			return sendLine(command, this.duration, this.unit, matchers);
		}

		public Map<Matcher<?>, Result> sendLine(String command, long duration, TimeUnit timeUnit, Matcher<?>... matchers) throws IOException {
			this.expect.sendLine(command);
			Map<Matcher<?>, Result> resultMap = expect(duration, timeUnit, matchers);
			return resultMap;
		}

		@Override
		public void close() throws IOException {
			try {
				this.process.waitFor(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
			}
			this.process.destroy();
			this.expect.close();
		}

		// ---

		private Map<Matcher<?>, Result> expect(long duration, TimeUnit timeUnit, Matcher<?>... matchers) throws IOException {
			Map<Matcher<?>, Result> resultMap = new TreeMap<>((key1, key2) -> key1 == key2 ? 0 : 1); // Utilization of object references of keys is enforced in the map operations throug using TreeMap with Comparator like here.
			if (matchers.length > 0) {
				resultMap.putAll(Arrays.stream(matchers).collect(Collectors.toMap(matcher -> matcher, matcher -> {
					try {
						return this.expect.withTimeout(duration, timeUnit).expect(matcher);
					} catch (IOException e) {
						return null;
					}
				})));
			} else {
				Matcher<Result> matcher = Matchers.regexp("\n$");
				resultMap.put(matcher, this.expect.withTimeout(duration, timeUnit).expect(matcher));
			}
			return resultMap;
		}
	}
}
