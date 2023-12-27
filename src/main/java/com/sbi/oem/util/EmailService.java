package com.sbi.oem.util;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	@Value("spring.mail.username")
	private String fromMail;

	@Autowired
	private JavaMailSender javaMailSender;

	public void sendMailMultipart(String toEmail, String[] cc, String subject, String message)
			throws MessagingException {

		MimeMessage mimeMessage = javaMailSender.createMimeMessage();

		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

		helper.setFrom(fromMail);
		helper.setTo(toEmail);
		helper.setCc(cc);

		helper.setSubject(subject);
		helper.setText(message, true);

		javaMailSender.send(mimeMessage);
	}

	public void sendMail(String toEmail, String[] cc, String subject, String message) throws MessagingException {
		sendMailMultipart(toEmail, cc, subject, message);
	}

	public void sendMailAndFile(String toEmail, String[] cc, String subject, String message, byte[] file,
			String fileName) throws MessagingException {

		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		try {

			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			helper.setFrom(fromMail);
			helper.setTo(toEmail);

			for (String ccRecipient : cc) {
				if (ccRecipient != null) {
					helper.addCc(ccRecipient);
				}	
			}

			helper.setSubject(subject);
			helper.setText(message, true);

			Multipart multipart = new MimeMultipart();

			MimeBodyPart textPart = new MimeBodyPart();
			textPart.setText(message, "utf-8", "html");
			multipart.addBodyPart(textPart);

			if (file != null && file.length > 0) {
				MimeBodyPart attachmentPart = new MimeBodyPart();
				DataSource source = new ByteArrayDataSource(file, "application/octet-stream");
				attachmentPart.setDataHandler(new DataHandler(source));
				attachmentPart.setFileName(fileName);
				multipart.addBodyPart(attachmentPart);
			}

			mimeMessage.setContent(multipart);

			javaMailSender.send(mimeMessage);

			
		} catch (MessagingException e) {
			
			e.printStackTrace();
			
		}
	}

	public void sendMail(String toEmail, String[] cc, String Subject, String message, byte[] file, String fileName)
			throws MessagingException {

		sendMailAndFile(toEmail, cc, Subject, message, file, fileName);
	}

}