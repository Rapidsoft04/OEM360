package com.sbi.oem.util;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	@Autowired
	private JavaMailSender javaMailSender;

	public void sendMailMultipart(String toEmail, String subject, String message) throws MessagingException {

		MimeMessage mimeMessage = javaMailSender.createMimeMessage();

		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

		helper.setFrom("vms.nyggs@gmail.com");
		helper.setTo(toEmail);

		helper.setSubject(subject);
		helper.setText(message, true);

		javaMailSender.send(mimeMessage);
	}

	public void sendMail(String toEmail, String subject, String message) throws MessagingException {
		sendMailMultipart(toEmail, subject, message);
	}

}