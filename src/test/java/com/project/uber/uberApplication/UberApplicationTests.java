package com.project.uber.uberApplication;

import com.project.uber.uberApplication.services.EmailSenderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UberApplicationTests {

	@Autowired
	private EmailSenderService emailSenderService;

	@Test
	void contextLoads() {
		emailSenderService.sendEmail(
				"sifamo3946@astimei.com",
				"This is a testing mail",
				"Body of my email"
		);
	}

	@Test
	void sendMultipleEmails() {

		String[] emails = {
				"sifamo3946@astimei.com",
				"ashuks.1018@gmail.com"
		};
		emailSenderService.sendEmail(
				emails,
				"This is a testing mail",
				"Body of my email"
		);
	}

}
