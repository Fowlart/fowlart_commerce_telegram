package com.fowlart.FowlartCommerce;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FowlartCommerceApplicationTests {

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
