package org.opentoolset.expect.design2;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentoolset.expect.AbstractTest;

import net.sf.expectit.Result;
import net.sf.expectit.matcher.Matcher;
import net.sf.expectit.matcher.Matchers;

public class TestShellExecutor extends AbstractTest {

	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

	@Test
	public void testExecutingSingleCommand() throws Exception {
		ShellExecutor.SessionBuilder sessionBuilder = new ShellExecutor.SessionBuilder();
		sessionBuilder.withDefaultTimeout(1, TimeUnit.SECONDS);
		try (ShellExecutor.Session session = sessionBuilder.create()) {
			session.sendLine("ls -la");
			Result result = session.getResult();
			Assertions.assertTrue(result.isSuccessful());
			System.out.println(result.getInput());
		}
	}

	@Test
	public void testManagingJavaKeystore() throws Exception {
		ShellExecutor.SessionBuilder sessionBuilder = new ShellExecutor.SessionBuilder();
		sessionBuilder.withShell("/bin/sh");
		sessionBuilder.withDefaultTimeout(1, TimeUnit.SECONDS);
		try (ShellExecutor.Session session = sessionBuilder.create()) {
			String command;
			Result result;

			Path path = Path.of(System.getProperty("user.home")).resolve("expect-test");
			command = String.format("mkdir -pv %s; echo $?", path);
			session.sendLine(command);
			System.err.println(command);
			result = session.expect(Matchers.exact("0\n"));
			System.out.println(result.getInput());
			Assertions.assertTrue(result.isSuccessful());

			command = String.format("cd %s; pwd", path);
			session.sendLine(command);
			System.err.println(command);
			result = session.expect(Matchers.contains(path.toString()));
			System.out.println(result.getInput());
			Assertions.assertTrue(result.isSuccessful());

			String now = dateTimeFormatter.format(LocalDateTime.now());
			String alias = String.format("test-%s", now);
			command = String.format("keytool -genkeypair -alias %s -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore test.p12 -validity 3650", alias);
			session.sendLine(command);
			System.err.println(command);
			result = session.expect(Matchers.contains("Enter keystore password:"));
			System.out.println(result.getInput());
			Assertions.assertTrue(result.isSuccessful());

			command = "secret";
			session.sendLine(command);
			System.err.println(command);
			{
				Matcher<Result> matcher1 = Matchers.contains("Re-enter new password:");
				Matcher<Result> matcher2 = Matchers.contains("What is your first and last name?");
				Map<Matcher<?>, Result> results = session.expect(matcher1, matcher2);
				if (results.get(matcher1).isSuccessful()) {
					result = results.get(matcher1);
					System.out.println(result.getInput());
					Assertions.assertTrue(result.isSuccessful());

					session.sendLine(command);
					System.err.println(command);
					result = session.expect(matcher2);
				} else if (results.get(matcher2).isSuccessful()) {
					result = results.get(matcher2);
				}
			}
			System.out.println(result.getInput());
			Assertions.assertTrue(result.isSuccessful());

			command = "name-1";
			session.sendLine(command);
			System.err.println(command);
			result = session.expect(Matchers.contains("What is the name of your organizational unit?"));
			System.out.println(result.getInput());
			Assertions.assertTrue(result.isSuccessful());

			command = "unit-1";
			session.sendLine(command);
			System.err.println(command);
			result = session.expect(Matchers.contains("What is the name of your organization?"));
			System.out.println(result.getInput());
			Assertions.assertTrue(result.isSuccessful());

			command = "org-1";
			session.sendLine(command);
			System.err.println(command);
			result = session.expect(Matchers.contains("What is the name of your City or Locality?"));
			System.out.println(result.getInput());
			Assertions.assertTrue(result.isSuccessful());

			command = "Ankara";
			session.sendLine(command);
			System.err.println(command);
			result = session.expect(Matchers.contains("What is the name of your State or Province?"));
			System.out.println(result.getInput());
			Assertions.assertTrue(result.isSuccessful());

			command = "Turkey";
			session.sendLine(command);
			System.err.println(command);
			result = session.expect(Matchers.contains("What is the two-letter country code for this unit?"));
			System.out.println(result.getInput());
			Assertions.assertTrue(result.isSuccessful());

			command = "TR";
			session.sendLine(command);
			System.err.println(command);
			result = session.expect(Matchers.regexp("Is .* correct"));
			System.out.println(result.getInput());
			Assertions.assertTrue(result.isSuccessful());

			command = "yes";
			session.sendLine(command);
			System.err.println(command);
			result = session.expect(Matchers.regexp("Generating .* key pair and self-signed certificate"));
			System.out.println(result.getInput());
			Assertions.assertTrue(result.isSuccessful());

			command = "keytool -list -keystore test.p12 | grep test";
			session.sendLine(command);
			System.err.println(command);
			result = session.expect(Matchers.contains("Enter keystore password:"));
			System.out.println(result.getInput());
			Assertions.assertTrue(result.isSuccessful());

			command = "secret";
			session.sendLine(command);
			System.err.println(command);
			result = session.expect(Matchers.contains(alias));
			System.out.println(result.getInput());
			Assertions.assertTrue(result.isSuccessful());
		}
	}
}
