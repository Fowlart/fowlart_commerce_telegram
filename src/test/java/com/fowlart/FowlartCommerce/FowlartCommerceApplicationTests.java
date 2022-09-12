package com.fowlart.FowlartCommerce;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootTest
class FowlartCommerceApplicationTests {

	private static final Logger log = LoggerFactory.getLogger(FowlartCommerceApplicationTests.class);

	@Value("${app.bot.userName}")
	private String userName;

	@Value("${app.bot.userName.token}")
	private String token;

	@Test
	void credsWillBeLoaded() {
		System.out.println(userName);
		System.out.println(token);
	}
}
