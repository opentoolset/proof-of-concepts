package org.opentoolset.expect.design1;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.MultiResult;
import net.sf.expectit.Result;
import net.sf.expectit.interact.InteractBuilder;
import net.sf.expectit.matcher.Matcher;

/**
 * Decorator class decorating the Expect implementation with shell execution support
 */
public class ExpectWithShellSession implements Expect {

	private String shell = "/bin/sh";
	private long duration = ExpectBuilder.DEFAULT_TIMEOUT_MS;
	private TimeUnit unit = TimeUnit.MILLISECONDS;

	private Process process;
	private Expect expect;

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
		return this.expect.expect(matcher);
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
	public void close() throws IOException {
		this.expect.close();
	}

	@Override
	public InteractBuilder interact() {
		return this.expect.interact();
	}

	@Override
	public InteractBuilder interactWith(int input) {
		return this.expect.interactWith(input);
	}

	// ---

	private void create() throws IOException {
		this.process = new ProcessBuilder(this.shell).start();

		ExpectBuilder expectBuilder = new ExpectBuilder();
		expectBuilder.withOutput(this.process.getOutputStream());
		expectBuilder.withInputs(this.process.getInputStream(), this.process.getErrorStream());
		expectBuilder.withCombineInputs(true);
		expectBuilder.withTimeout(this.duration, this.unit);

		this.expect = expectBuilder.build();
	}

	// ---

	public static class Builder {

		private ExpectWithShellSession shellSession = new ExpectWithShellSession();

		public Builder withShell(String shell) {
			this.shellSession.shell = shell;
			return this;
		}

		public Builder withDefaultTimeout(long duration, TimeUnit unit) {
			this.shellSession.duration = duration;
			this.shellSession.unit = unit;
			return this;
		}

		public ExpectWithShellSession create() throws IOException {
			this.shellSession.create();
			return this.shellSession;
		}
	}
}
