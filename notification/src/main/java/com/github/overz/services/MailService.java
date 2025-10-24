package com.github.overz.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MailService {
	public void sendOrderNotification(final String to, final String orderId) {
		// In this environment, an external MailHog is used. For simplicity, we just log the email action.
		log.info("[MAIL] (mock) Would send email to '{}' about orderId='{}'", to, orderId);
	}
}
