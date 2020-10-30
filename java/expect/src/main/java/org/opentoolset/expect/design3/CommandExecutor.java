package org.opentoolset.expect.design3;

import java.io.Closeable;
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
import net.sf.expectit.Result;
import net.sf.expectit.filter.Filter;
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

		public SessionCreator withDefaultTimeout(long duration, TimeUnit unit) {
			this.session.duration = duration;
			this.session.unit = unit;
			return this;
		}

		public SessionCreator withInputFilters(Filter filter, Filter... moreFilters) {
			this.session.inputFilters = new ArrayList<>();
			this.session.inputFilters.add(filter);
			this.session.inputFilters.addAll(Arrays.asList(moreFilters));
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

	public static class Session implements Closeable {

		protected OutputStream outputStream;
		protected InputStream[] inputStreams;
		protected long duration = ExpectBuilder.DEFAULT_TIMEOUT_MS;
		protected TimeUnit unit = TimeUnit.MILLISECONDS;
		private List<Filter> inputFilters;

		protected Expect expect;

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
		}

		// ---

		protected void create() throws IOException {
			ExpectBuilder expectBuilder = new ExpectBuilder();
			expectBuilder.withOutput(this.outputStream);
			expectBuilder.withInputs(this.inputStreams);
			expectBuilder.withCombineInputs(true);
			expectBuilder.withTimeout(this.duration, this.unit);

			if (this.inputFilters != null && this.inputFilters.size() > 0) {
				Filter[] moreInputFilters = this.inputFilters.size() > 1 ? this.inputFilters.subList(1, this.inputFilters.size()).toArray(new Filter[] { }) : new Filter[0];
				expectBuilder.withInputFilters(this.inputFilters.get(0), moreInputFilters);
			}

			this.expect = expectBuilder.build();
		}
	}
}
