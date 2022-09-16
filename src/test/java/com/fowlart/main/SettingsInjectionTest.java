package com.fowlart.main;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Objects;

@SpringBootTest
class SettingsInjectionTest {

	private static final Logger log = LoggerFactory.getLogger(SettingsInjectionTest.class);

	@Value("${app.bot.userName}")
	private String userName;

	@Value("${app.bot.userName.token}")
	private String token;

	@Test
	void credentialsDownloading() {
		assert (Objects.nonNull(userName));
		assert (Objects.nonNull(token));
	}
}
