package org.opentoolset.expect;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

public abstract class AbstractTest {

	@BeforeAll
	public static void beforeAll(TestInfo testInfo) {
		System.out.printf("\n---\n---\nExecuting: %s\n", testInfo.getTestClass().orElseThrow().getName());
	}

	@AfterAll
	public static void afterAll(TestInfo testInfo) {
		System.out.printf("\n---\n---\nFinished: %s\n", testInfo.getTestClass().orElseThrow().getName());
	}

	@BeforeEach
	public void before(TestInfo testInfo) {
		System.out.printf("\n---\nExecuting: %s\n---\n", testInfo.getTestMethod().orElseThrow().getName());
	}

	@AfterEach
	public void after(TestInfo testInfo) {
		System.out.printf("\n---\nFinished: %s\n---\n", testInfo.getTestMethod().orElseThrow().getName());
	}
}
