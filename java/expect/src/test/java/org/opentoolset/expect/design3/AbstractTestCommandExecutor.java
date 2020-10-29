package org.opentoolset.expect.design3;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.opentoolset.expect.AbstractTest;

import net.sf.expectit.Result;
import net.sf.expectit.matcher.Matcher;
import net.sf.expectit.matcher.Matchers;

public class AbstractTestCommandExecutor extends AbstractTest {

	private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

	protected void testExecutingSingleCommand(CommandExecutor.SessionBuilder sessionBuilder) throws IOException {
		try (CommandExecutor.Session session = sessionBuilder.create()) {
			Result result = session.sendLine("ls -la");
			Assertions.assertTrue(result.isSuccessful());
			System.out.println(result.getInput());
		}
	}

	protected void testManagingJavaKeystore(CommandExecutor.SessionBuilder sessionBuilder) throws IOException {
		sessionBuilder.withDefaultTimeout(1, TimeUnit.SECONDS);
		try (CommandExecutor.Session session = sessionBuilder.create()) {
			Path path = Path.of(System.getProperty("user.home")).resolve("expect-test");
			Result result;
			result = session.sendLine(String.format("mkdir -pv %s; pwd", path));
			result = session.sendLine(String.format("cd %s; pwd", path), Matchers.contains(path.toString()));
			Assertions.assertTrue(result.isSuccessful());

			String now = dateTimeFormatter.format(LocalDateTime.now());
			String alias = String.format("test-%s", now);
			String command = String.format("keytool -genkeypair -alias %s -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore test.p12 -validity 3650", alias);
			result = session.sendLine(command, Matchers.contains("Enter keystore password:"));
			Assertions.assertTrue(result.isSuccessful());

			{ // Multi-matcher sample:
				Matcher<Result> matcher1 = Matchers.contains("Re-enter new password:");
				Matcher<Result> matcher2 = Matchers.contains("What is your first and last name?");
				Map<Matcher<?>, Result> results = session.sendLine("secret", matcher1, matcher2);
				if (results.get(matcher1).isSuccessful()) {
					result = session.sendLine("secret", matcher2);
					Assertions.assertTrue(result.isSuccessful());
				} else if (results.get(matcher2).isSuccessful()) {
					Assertions.assertTrue(result.isSuccessful());
				}
			}

			result = session.sendLine("name-1", Matchers.contains("What is the name of your organizational unit?"));
			Assertions.assertTrue(result.isSuccessful());

			result = session.sendLine("unit-1", Matchers.contains("What is the name of your organization?"));
			Assertions.assertTrue(result.isSuccessful());

			result = session.sendLine("org-1", Matchers.contains("What is the name of your City or Locality?"));
			Assertions.assertTrue(result.isSuccessful());

			result = session.sendLine("Ankara", Matchers.contains("What is the name of your State or Province?"));
			Assertions.assertTrue(result.isSuccessful());

			result = session.sendLine("Turkey", Matchers.contains("What is the two-letter country code for this unit?"));
			Assertions.assertTrue(result.isSuccessful());

			result = session.sendLine("TR", Matchers.regexp("Is .* correct"));
			Assertions.assertTrue(result.isSuccessful());

			result = session.sendLine("yes", Matchers.regexp("Generating .* key pair and self-signed certificate"));
			Assertions.assertTrue(result.isSuccessful());

			result = session.sendLine("keytool -list -keystore test.p12 | grep test", Matchers.contains("Enter keystore password:"));
			Assertions.assertTrue(result.isSuccessful());

			result = session.sendLine("secret", Matchers.contains(alias));
			Assertions.assertTrue(result.isSuccessful());

			System.out.printf("New cert alias: %s\n", alias);
			System.out.printf("Last command output:\n%s\n", result.getInput().trim());
		}
	}
}