package org.opentoolset.expect.old;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
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

		private final Supplier<Matcher<Result>> defaultMatcherProvider = () -> Matchers.anyString();
		// private final Supplier<Matcher<Result>> defaultMatcherProvider = () -> Matchers.regexp(Pattern.compile("^.*$", Pattern.MULTILINE | Pattern.DOTALL));

		public Expect getExpect() {
			return this.expect;
		}

		public Result getResult() throws IOException {
			Matcher<Result> matcher = this.defaultMatcherProvider.get();
			Result result = this.expect.expect(matcher);
			return result;
		}

		public void sendLine(String command) throws IOException {
			this.expect.sendLine(command);
		}

		public Result expect(Matcher<?> matcher) throws IOException {
			Result result = expect(this.duration, this.unit, matcher);
			return result;
		}

		public Map<Matcher<?>, Result> expect(Matcher<?>... matchers) throws IOException {
			return expect(this.duration, this.unit, matchers);
		}

		public Result expect(long duration, TimeUnit timeUnit, Matcher<?> matcher) {
			try {
				Result result = this.expect.withTimeout(duration, timeUnit).expect(matcher);
				return result;
			} catch (IOException e) {
				return null;
			}
		}

		public Map<Matcher<?>, Result> expect(long duration, TimeUnit timeUnit, Matcher<?>... matchers) throws IOException {
			Map<Matcher<?>, Result> resultMap = new TreeMap<>((key1, key2) -> key1 == key2 ? 0 : 1); // Utilization of object references of keys is enforced in the map operations throug using TreeMap with Comparator like here.
			if (matchers.length > 0) {
				Function<Matcher<?>, Result> valueMapper = matcher -> expect(duration, timeUnit, matcher);
				resultMap.putAll(Arrays.stream(matchers).collect(Collectors.toMap(matcher -> matcher, valueMapper)));
			} else {
				Matcher<Result> matcher = this.defaultMatcherProvider.get();
				Result result = expect(duration, timeUnit, matcher);
				resultMap.put(matcher, result);
			}
			return resultMap;
		}

		@Override
		public void close() throws IOException {
			this.expect.close();
			try {
				this.process.waitFor(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
			}
			this.process.destroy();
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
	}
}
