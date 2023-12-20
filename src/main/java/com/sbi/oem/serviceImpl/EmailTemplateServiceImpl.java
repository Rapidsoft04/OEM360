package com.sbi.oem.serviceImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sbi.oem.dto.Response;
import com.sbi.oem.enums.PriorityEnum;
import com.sbi.oem.model.Component;
import com.sbi.oem.model.DepartmentApprover;
import com.sbi.oem.model.Recommendation;
import com.sbi.oem.model.RecommendationDeplyomentDetails;
import com.sbi.oem.model.RecommendationStatus;
import com.sbi.oem.model.RecommendationType;
import com.sbi.oem.repository.ComponentRepository;
import com.sbi.oem.repository.DepartmentApproverRepository;
import com.sbi.oem.repository.RecommendationTypeRepository;
import com.sbi.oem.service.EmailTemplateService;
import com.sbi.oem.service.RecommendationService;
import com.sbi.oem.util.EmailService;

@Service
public class EmailTemplateServiceImpl implements EmailTemplateService {

	@Autowired
	private EmailService emailService;

	@Autowired
	private DepartmentApproverRepository departmentApproverRepository;

	@Autowired
	private ComponentRepository componentRepository;

	@Autowired
	private RecommendationTypeRepository recommendationTypeRepository;

	@Override
	public Response<?> sendMail(Recommendation recommendation) {

		try {

			CompletableFuture.runAsync(() -> {

				try {

					Optional<DepartmentApprover> userDepartment = departmentApproverRepository
							.findAllByDepartmentId(recommendation.getDepartment().getId());

					Optional<Component> userComponent = componentRepository
							.findById(recommendation.getComponent().getId());
					Optional<RecommendationType> userRecommendationType = recommendationTypeRepository
							.findById(recommendation.getRecommendationType().getId());

					String priority = "";
					if (recommendation.getPriorityId().longValue() == 1) {
						priority = PriorityEnum.High.getName();
					} else if (recommendation.getPriorityId().longValue() == 2) {
						priority = PriorityEnum.Medium.getName();
					} else {
						priority = PriorityEnum.Low.getName();
					}

					Date recommendDate = recommendation.getRecommendDate();
					LocalDate localDate = recommendDate.toInstant().atZone(java.time.ZoneId.systemDefault())
							.toLocalDate();
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
					String formattedDate = localDate.format(formatter);

					byte[] userRecommendationfile = convertMultipartFileToBytes(recommendation.getFileUrl());
					String fileName = recommendation.getReferenceId();

					String agmEmail = userDepartment.get().getAgm().getEmail();
					String applicationOwnerEmail = userDepartment.get().getApplicationOwner().getEmail();
					String[] ccEmails = { applicationOwnerEmail };

//				String mailSubject ="";
//				if(recommendation.getRecommendationStatus().getId() ==1) {
//					 mailSubject = "OEM Recommendation Request";
//				}else if(recommendation.getRecommendationStatus().getId() == 3) {
//					 mailSubject = "OEM Recommendation Approved";
//				}else if(recommendation.getRecommendationStatus().getId() ==4) {
//					 mailSubject = "OEM Recommendation Rejected";
//				}

					String mailSubject = "OEM Recommendation Request";

					String content = String.format("<div style='background-color: #f4f4f4; padding: 20px;'>"
							+ "<div style='max-width: 1200px; margin: 0 auto; background-color: #ffffff; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);'>"
							+ "<h1 style='font-size: 24px; color: #333; font-weight: bold; '>OEM Recommendation Request</h1>"
							+ "<p style='font-size: 16px; color: #555;  '><b> Reference Id : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #555;  '><b> Recommendation Type : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #555;  '><b> Priority Type : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #555;  '><b> Descriptions : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #555;  '><b> Department Name :</b> %s</p>"
							+ "<p style='font-size: 16px; color: #555;  '><b> Component Name : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #555;  '><b> Recommend Date : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #555;  '><b> Expected Impact : </b>%s</p>" + "</div>"
							+ "</div>",

							recommendation.getReferenceId(), userRecommendationType.get().getName(), priority,
							recommendation.getDescriptions(), userDepartment.get().getDepartment().getName(),
							userComponent.get().getName(), formattedDate,
							recommendation.getExpectedImpact() != null ? recommendation.getExpectedImpact() : "NA");

					emailService.sendMailAndFile(agmEmail, ccEmails, mailSubject, content, userRecommendationfile,
							fileName);
					System.out.println("Mail sent to AGM successfully!!");

//	                    emailService.sendMail(applicationOwnerEmail, mailSubject, content);
//	                    System.out.println("Mail sent to Application Owner successfully!!");

				} catch (MessagingException e) {
					e.printStackTrace();
				}

			});

		} catch (Exception e) {

			e.printStackTrace();
			return new Response<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to send email", null);
		}

		return new Response<>(HttpStatus.OK.value(), "Mail Send Successfully", null);
	}

	private byte[] convertMultipartFileToBytes(String fileUrl) {

		if (fileUrl == null || fileUrl.isEmpty()) {
			return null;
		}

		try {
			URL url = new URL(fileUrl);
			URLConnection connection = url.openConnection();

			try (InputStream inputStream = connection.getInputStream();
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

				byte[] buffer = new byte[4096];
				int bytesRead;

				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}

				return outputStream.toByteArray();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Response<?> sendMail(RecommendationDeplyomentDetails details, Optional<Recommendation> recommendation) {
		try {

			CompletableFuture.runAsync(() -> {

				try {

					Optional<DepartmentApprover> userDepartment = departmentApproverRepository
							.findAllByDepartmentId(recommendation.get().getDepartment().getId());

					String agmEmail = userDepartment.get().getAgm().getEmail();

					String mailSubject = "AppOwner Approval";

					String content = String.format("<div style='background-color: #f4f4f4; padding: 20px;'>"
							+ "<div style='max-width: 1200px; margin: 0 auto; background-color: #ffffff; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);'>"
							+ "<h1 style='font-size: 24px; color: #333; font-weight: bold; '>OEM Recommended Request Accepted</h1>"
							+ "<p style='font-size: 16px; color: #555;  '><b> Reference Id : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #555;  '><b> Development Start Date : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #555;  '><b> Development End Date : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #555;  '><b> Test Completion Date : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #555;  '><b> Deployment Date : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #555;  '><b> Impacted Department : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #555;  '><b> Global Support Number : </b>%s</p>"
							+ "</div>",

							details.getRecommendRefId(), formatDate(details.getDevelopmentStartDate()),
							formatDate(details.getDevelopementEndDate()), formatDate(details.getTestCompletionDate()),
							formatDate(details.getDeploymentDate()), details.getImpactedDepartment(),
							details.getGlobalSupportNumber() != null ? details.getGlobalSupportNumber() : "NA"

					);

					emailService.sendMail(agmEmail, agmEmail, mailSubject, content);
					System.out.println("Mail sent to AGM successfully!!");

				} catch (MessagingException e) {
					e.printStackTrace();
				}

			});

		} catch (Exception e) {

			e.printStackTrace();
			return new Response<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to send email", null);
		}

		return new Response<>(HttpStatus.OK.value(), "Mail Send Successfully", null);

	}

	public static String formatDate(Date date) {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		Instant instant = date.toInstant();
		LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
		return localDate.format(dateFormatter);
	}

}
