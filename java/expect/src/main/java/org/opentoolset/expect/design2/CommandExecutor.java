package org.opentoolset.expect.design2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.MultiResult;
import net.sf.expectit.Result;
import net.sf.expectit.filter.Filter;
import net.sf.expectit.interact.InteractBuilder;
import net.sf.expectit.matcher.Matcher;
import net.sf.expectit.matcher.Matchers;

public class CommandExecutor {

	public static class SessionCreator {

		protected Session session = buildSession();

		public SessionCreator withOutput(OutputStream outputStream) {
			this.session.outputStream = outputStream;
			return this;
		}

		public SessionCreator withInputs(InputStream... inputStreams) {
			this.session.inputStreams = inputStreams;
			return this;
		}

		public SessionCreator withDefaultTimeout(long duration, TimeUnit timeUnit) {
			this.session.setDefaultTimeut(duration, timeUnit);
			return this;
		}

		public SessionCreator withInputFilters(Filter filter, Filter... moreFilters) {
			this.session.inputFilters = new ArrayList<>();
			this.session.inputFilters.add(filter);
			this.session.inputFilters.addAll(Arrays.asList(moreFilters));
			return this;
		}

		public SessionCreator withDefaultMatcherProvider(Supplier<Matcher<Result>> defaultMatcherProvider) {
			this.session.defaultMatcherProvider = defaultMatcherProvider;
			return this;
		}

		public SessionCreator withEchoOutput(Appendable echoOutput) {
			this.session.echoOutput = echoOutput;
			return this;
		}

		public SessionCreator withEchoInputs(Appendable echoInput, Appendable... otherEchoInputs) {
			this.session.echoInput = echoInput;
			this.session.otherEchoInputs = otherEchoInputs;
			return this;
		}

		public Session create() throws Exception {
			this.session.create();
			return session;
		}

		protected Session buildSession() {
			return new Session();
		}

		protected Session getSession() {
			return session;
		}
	}

	public static class Session implements Expect {

		private OutputStream outputStream;
		private InputStream[] inputStreams = new InputStream[] { };
		private List<Filter> inputFilters;

		private long duration;
		private TimeUnit timeUnit;
		private long durationNanos;

		private Appendable echoOutput;
		private Appendable echoInput;
		private Appendable[] otherEchoInputs;

		private Supplier<Matcher<Result>> defaultMatcherProvider = () -> Matchers.eof();

		private Expect expect;

		public Session() {
			setDefaultTimeut(ExpectBuilder.DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		}

		public Result getResult() throws IOException {
			Matcher<Result> matcher = this.defaultMatcherProvider.get();
			Result result = this.expect.expect(matcher);
			return result;
		}

		public void send(String format, Object... args) throws IOException {
			String command = String.format(format, args);
			this.expect.send(command);
		}

		public void sendLine(String format, Object... args) throws IOException {
			String command = String.format(format, args);
			this.expect.sendLine(command);
		}

		public <R extends Result> R expect(long duration, TimeUnit timeUnit, Matcher<R> matcher) throws IOException {
			if (this.durationNanos == timeUnit.toNanos(duration)) {
				return this.expect.expect(matcher);
			} else {
				return this.expect.withTimeout(duration, timeUnit).expect(matcher);
			}
		}

		public Map<Matcher<?>, Result> expectAsMap(Matcher<?>... matchers) throws IOException {
			return expect(this.duration, this.timeUnit, matchers);
		}

		public Map<Matcher<?>, Result> expect(long duration, TimeUnit timeUnit, Matcher<?>... matchers) throws IOException {
			Map<Matcher<?>, Result> resultMap = new TreeMap<>((key1, key2) -> key1 == key2 ? 0 : 1); // Utilization of object references of keys is enforced in the map operations throug using TreeMap with Comparator like here.
			if (matchers.length > 0) {
				Function<Matcher<?>, Result> valueMapper = matcher -> {
					try {
						Result result = expect(duration, timeUnit, matcher);
						return result;
					} catch (IOException e) {
						return null;
					}
				};

				resultMap.putAll(Arrays.stream(matchers).collect(Collectors.toMap(matcher -> matcher, valueMapper)));
			} else {
				Matcher<Result> matcher = this.defaultMatcherProvider.get();
				Result result = expect(duration, timeUnit, matcher);
				resultMap.put(matcher, result);
			}
			return resultMap;
		}

		@Override
		public Expect send(String string) throws IOException {
			return this.expect.send(string);
		}

		@Override
		public Expect sendLine(String string) throws IOException {
			return this.expect.sendLine(string);
		}

		@Override
		public Expect sendLine() throws IOException {
			return this.expect.sendLine();
		}

		@Override
		public Expect sendBytes(byte[] bytes) throws IOException {
			return this.expect.sendBytes(bytes);
		}

		@Override
		public <R extends Result> R expect(Matcher<R> matcher) throws IOException {
			R result = this.expect(matcher);
			return result;
		}

		@Override
		public MultiResult expect(Matcher<?>... matchers) throws IOException {
			return this.expect.expect(matchers);
		}

		@Override
		@Deprecated
		public <R extends Result> R expect(long timeoutMs, Matcher<R> matcher) throws IOException {
			return this.expect.expect(timeoutMs, matcher);
		}

		@Override
		@Deprecated
		public MultiResult expect(long timeoutMs, Matcher<?>... matchers) throws IOException {
			return this.expect.expect(timeoutMs, matchers);
		}

		@Override
		public <R extends Result> R expectIn(int input, Matcher<R> matcher) throws IOException {
			return this.expect.expectIn(input, matcher);
		}

		@Override
		@Deprecated
		public <R extends Result> R expectIn(int input, long timeoutMs, Matcher<R> matcher) throws IOException {
			return this.expect.expectIn(input, timeoutMs, matcher);
		}

		@Override
		@Deprecated
		public MultiResult expectIn(int input, long timeoutMs, Matcher<?>... matchers) throws IOException {
			return this.expect.expectIn(input, timeoutMs, matchers);
		}

		@Override
		public Expect withTimeout(long duration, TimeUnit unit) {
			return this.expect.withTimeout(duration, unit);
		}

		@Override
		@Deprecated
		public Expect withInfiniteTimeout() {
			return this.expect.withInfiniteTimeout();
		}

		@Override
		public InteractBuilder interact() {
			return this.expect.interact();
		}

		@Override
		public InteractBuilder interactWith(int input) {
			return this.expect.interactWith(input);
		}

		@Override
		public void close() throws IOException {
			this.expect.close();
		}

		// ---

		protected void create() throws IOException {
			ExpectBuilder expectBuilder = new ExpectBuilder();
			expectBuilder.withOutput(this.outputStream);
			expectBuilder.withInputs(this.inputStreams);
			expectBuilder.withCombineInputs(true);
			expectBuilder.withTimeout(this.duration, this.timeUnit);
			if (this.inputFilters != null && this.inputFilters.size() > 0) {
				Filter[] moreInputFilters = this.inputFilters.size() > 1 ? this.inputFilters.subList(1, this.inputFilters.size()).toArray(new Filter[] { }) : new Filter[0];
				expectBuilder.withInputFilters(this.inputFilters.get(0), moreInputFilters);
			}

			expectBuilder.withEchoOutput(this.echoOutput);
			expectBuilder.withEchoInput(this.echoInput, this.otherEchoInputs);

			this.expect = expectBuilder.build();
		}

		private void setDefaultTimeut(long duration, TimeUnit timeUnit) {
			this.duration = duration;
			this.timeUnit = timeUnit;
			this.durationNanos = timeUnit.toNanos(duration);
		}
	}
}
