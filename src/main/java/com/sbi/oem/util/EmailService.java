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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	@Value("spring.mail.username")
	private String fromMail;

	@Autowired
	private JavaMailSender javaMailSender;

	public void sendMailMultipart(String toEmail, String cc, String subject, String message) throws MessagingException {

		MimeMessage mimeMessage = javaMailSender.createMimeMessage();

		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

		helper.setFrom(fromMail);
		helper.setTo(toEmail);
		helper.setCc(cc);

		helper.setSubject(subject);
		helper.setText(message, true);

		javaMailSender.send(mimeMessage);
	}

	public void sendMail(String toEmail, String cc, String subject, String message) throws MessagingException {
		sendMailMultipart(toEmail, cc, subject, message);
	}

	public void sendMailAndFile(String toEmail, String[] cc, String subject, String message, byte[] file, String fileName)
			throws MessagingException {

		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		try {

			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			helper.setFrom(fromMail);
			helper.setTo(toEmail);
			helper.setCc(cc);

			helper.setSubject(subject);
			helper.setText(message, true);
			
			MimeBodyPart textPart = new MimeBodyPart();
	        textPart.setText(message, "utf-8", "html");

//	    // Attach the file
//	    if (file != null && fileName != null) {
//	        helper.addAttachment(fileName, new ByteArrayResource(file));
//	    }

			MimeBodyPart attachmentPart = new MimeBodyPart();
			DataSource source = new ByteArrayDataSource(file, "application/octet-stream");
			attachmentPart.setDataHandler(new DataHandler(source));
			attachmentPart.setFileName(fileName);

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(textPart);
			multipart.addBodyPart(attachmentPart);
			mimeMessage.setContent(multipart);

			javaMailSender.send(mimeMessage);

			System.out.println("Mail sent successfully");
		} catch (MessagingException e) {
			e.printStackTrace();
			System.out.println("Failed to send email: " + e.getMessage());
		}
	}

	public void sendMail(String toEmail, String[] cc, String Subject, String message, byte[] file, String fileName)
			throws MessagingException {

		sendMailAndFile(toEmail, cc, Subject, message, file, fileName);
	}

}