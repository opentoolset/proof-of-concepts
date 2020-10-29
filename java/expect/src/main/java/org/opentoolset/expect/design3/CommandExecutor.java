package org.opentoolset.expect.design3;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

public class CommandExecutor {

	public static class SessionBuilder {

		protected OutputStream outputStream;
		protected InputStream inputStream;
		protected InputStream errorStream;
		protected long duration = ExpectBuilder.DEFAULT_TIMEOUT_MS;
		protected TimeUnit unit = TimeUnit.MILLISECONDS;

		public SessionBuilder withOutputStream(OutputStream outputStream) {
			this.outputStream = outputStream;
			return this;
		}

		public SessionBuilder withInputStream(InputStream inputStream) {
			this.inputStream = inputStream;
			return this;
		}

		public SessionBuilder withErrorStream(InputStream errorStream) {
			this.errorStream = errorStream;
			return this;
		}

		public SessionBuilder withDefaultTimeout(long duration, TimeUnit unit) {
			this.duration = duration;
			this.unit = unit;
			return this;
		}

		public Session create() throws IOException {
			Session session = new Session();
			prepare(session);
			return session;
		}

		protected void prepare(Session session) throws IOException {
			session.outputStream = this.outputStream;
			session.inputStream = this.inputStream;
			session.errorStream = this.errorStream;
			session.duration = this.duration;
			session.unit = this.unit;
			session.create();
		}
	}

	public static class Session implements Closeable {

		protected OutputStream outputStream;
		protected InputStream inputStream;
		protected InputStream errorStream;
		protected long duration;
		protected TimeUnit unit;

		protected Expect expect;

		protected void create() throws IOException {
			ExpectBuilder expectBuilder = new ExpectBuilder();
			expectBuilder.withOutput(this.outputStream);
			expectBuilder.withInputs(this.inputStream, this.errorStream);
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
