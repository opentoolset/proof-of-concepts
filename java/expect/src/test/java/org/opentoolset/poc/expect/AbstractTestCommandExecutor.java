package org.opentoolset.poc.expect;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;

import net.sf.expectit.Result;
import net.sf.expectit.interact.InteractBuilder;
import net.sf.expectit.matcher.Matcher;
import net.sf.expectit.matcher.Matchers;

public abstract class AbstractTestCommandExecutor extends AbstractTest {

	private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

	protected void testExecutingCommands(CommandExecutor.SessionCreator sessionBuilder) throws Exception {
		sessionBuilder.withDefaultTimeout(1, TimeUnit.SECONDS);
		try (CommandExecutor.Session session = sessionBuilder.create()) {
			Result result;

			result = session.getResult(); // Result succeeds for SSH, but fails for local shell. So we couldn't check the result here.

			session.sendLine("cd %s; pwd", Path.of(System.getProperty("user.home")));
			result = session.getResult();
			Assertions.assertTrue(result.isSuccessful());

			session.sendLine("ls -l");
			result = session.getResult();
			Assertions.assertTrue(result.isSuccessful());

			session.sendLine("exit");
		}
	}

	protected void testManagingJavaKeystore(CommandExecutor.SessionCreator sessionBuilder) throws Exception {
		try (CommandExecutor.Session session = sessionBuilder.create()) {
			Result result;

			result = session.getResult(); // Result succeeds for SSH, but fails for local shell. So we couldn't check the result here.

			Path path = Path.of(System.getProperty("user.home")).resolve("expect-test");
			session.sendLine("mkdir -pv %s; echo $?", path);
			result = session.expect(Matchers.regexp("0\r?\n"));
			Assertions.assertTrue(result.isSuccessful());

			session.sendLine("cd %s; pwd", path);
			result = session.expect(Matchers.contains(path.toString()));
			Assertions.assertTrue(result.isSuccessful());

			String now = dateTimeFormatter.format(LocalDateTime.now());
			session.sendLine("keytool -genkeypair -alias test-%s -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore test.p12 -validity 3650", now);
			result = session.expect(Matchers.contains("Enter keystore password:"));
			Assertions.assertTrue(result.isSuccessful());

			session.sendLine("secret");
			{
				Matcher<Result> matcher1 = Matchers.contains("Re-enter new password:");
				Matcher<Result> matcher2 = Matchers.contains("What is your first and last name?");
				Map<Matcher<?>, Result> results = session.expectAsMap(matcher1, matcher2);
				if (results.get(matcher1).isSuccessful()) {
					result = results.get(matcher1);
					Assertions.assertTrue(result.isSuccessful());

					session.sendLine("secret");
					result = session.expect(matcher2);
				} else if (results.get(matcher2).isSuccessful()) {
					result = results.get(matcher2);
				}
			}
			Assertions.assertTrue(result.isSuccessful());

			session.sendLine("name-1");
			result = session.expect(Matchers.contains("What is the name of your organizational unit?"));
			Assertions.assertTrue(result.isSuccessful());

			session.sendLine("unit-1");
			result = session.expect(Matchers.contains("What is the name of your organization?"));
			Assertions.assertTrue(result.isSuccessful());

			session.sendLine("org-1");
			result = session.expect(Matchers.contains("What is the name of your City or Locality?"));
			Assertions.assertTrue(result.isSuccessful());

			session.sendLine("Ankara");
			result = session.expect(Matchers.contains("What is the name of your State or Province?"));
			Assertions.assertTrue(result.isSuccessful());

			session.sendLine("Turkey");
			result = session.expect(Matchers.contains("What is the two-letter country code for this unit?"));
			Assertions.assertTrue(result.isSuccessful());

			session.sendLine("TR");
			result = session.expect(Matchers.regexp("Is .* correct"));
			Assertions.assertTrue(result.isSuccessful());

			session.sendLine("yes");
			result = session.expect(Matchers.regexp("Generating .* key pair and self-signed certificate"));
			Assertions.assertTrue(result.isSuccessful());

			session.sendLine("keytool -list -keystore test.p12 | grep test");
			result = session.expect(Matchers.contains("Enter keystore password:"));
			Assertions.assertTrue(result.isSuccessful());

			session.sendLine("secret");
			result = session.expect(Matchers.contains(now));
			Assertions.assertTrue(result.isSuccessful());

			session.sendLine("exit");
		}
	}

	protected void testManagingJavaKeystoreDeclaratively(CommandExecutor.SessionCreator sessionBuilder) throws Exception {
		try (CommandExecutor.Session session = sessionBuilder.create()) {
			session.getResult(); // Result succeeds for SSH, but fails for local shell. So we couldn't check the result here.

			Path path = Path.of(System.getProperty("user.home")).resolve("expect-test");
			session.sendLine("mkdir -pv %s; echo $?", path);
			Assertions.assertTrue(session.expect(Matchers.regexp("0\r?\n")).isSuccessful());

			session.sendLine("cd %s; pwd", path);
			Assertions.assertTrue(session.expect(Matchers.contains(path.toString())).isSuccessful());

			String now = dateTimeFormatter.format(LocalDateTime.now());
			session.sendLine("keytool -genkeypair -alias test-%s -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore test.p12 -validity 3650", now);

			InteractBuilder interactBuilder = session.interact();
			{
				interactBuilder.when(Matchers.contains("Enter keystore password:")).then(result -> session.sendLine("secret"));
				interactBuilder.when(Matchers.contains("Re-enter new password:")).then(result -> session.sendLine("secret"));
				interactBuilder.when(Matchers.contains("What is your first and last name?")).then(result -> session.sendLine("name-1"));
				interactBuilder.when(Matchers.contains("What is the name of your organizational unit?")).then(result -> session.sendLine("unit-1"));
				interactBuilder.when(Matchers.contains("What is the name of your organization?")).then(result -> session.sendLine("org-1"));
				interactBuilder.when(Matchers.contains("What is the name of your City or Locality?")).then(result -> session.sendLine("Ankara"));
				interactBuilder.when(Matchers.contains("What is the name of your State or Province?")).then(result -> session.sendLine("Turkey"));
				interactBuilder.when(Matchers.contains("What is the two-letter country code for this unit?")).then(result -> session.sendLine("TR"));
				interactBuilder.when(Matchers.regexp("Is .* correct")).then(result -> session.sendLine("yes"));
			}

			interactBuilder.until(Matchers.regexp("Generating .* key pair and self-signed certificate"));

			session.sendLine("keytool -list -keystore test.p12 | grep test");
			Assertions.assertTrue(session.expect(Matchers.contains("Enter keystore password:")).isSuccessful());

			session.sendLine("secret");
			Assertions.assertTrue(session.expect(Matchers.contains(now)).isSuccessful());

			session.sendLine("exit");
		}
	}
}
