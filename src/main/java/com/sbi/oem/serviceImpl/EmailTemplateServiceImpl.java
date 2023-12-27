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
import com.sbi.oem.enums.RecommendationStatusEnum;
import com.sbi.oem.model.Component;
import com.sbi.oem.model.DepartmentApprover;
import com.sbi.oem.model.Recommendation;
import com.sbi.oem.model.RecommendationDeplyomentDetails;
import com.sbi.oem.model.RecommendationMessages;
import com.sbi.oem.model.RecommendationType;
import com.sbi.oem.model.User;
import com.sbi.oem.repository.ComponentRepository;
import com.sbi.oem.repository.DepartmentApproverRepository;
import com.sbi.oem.repository.RecommendationRepository;
import com.sbi.oem.repository.RecommendationTypeRepository;
import com.sbi.oem.repository.UserRepository;
import com.sbi.oem.service.EmailTemplateService;
import com.sbi.oem.util.EmailService;

@Service
public class EmailTemplateServiceImpl implements EmailTemplateService {

	@Autowired
	private EmailService emailService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DepartmentApproverRepository departmentApproverRepository;

	@Autowired
	private ComponentRepository componentRepository;

	@Autowired
	private RecommendationTypeRepository recommendationTypeRepository;

	@Autowired
	private RecommendationRepository recommendationRepository;

	@Override
	public Response<?> sendMailRecommendation(Recommendation recommendation, RecommendationStatusEnum status) {

		try {

			CompletableFuture.runAsync(() -> {

				try {

					Optional<DepartmentApprover> userDepartment = departmentApproverRepository
							.findAllByDepartmentId(recommendation.getDepartment().getId());

					Optional<Component> userComponent = componentRepository
							.findById(recommendation.getComponent().getId());

					Optional<RecommendationType> userRecommendationType = recommendationTypeRepository
							.findById(recommendation.getRecommendationType().getId());

					Optional<User> user = userRepository.findById(recommendation.getCreatedBy().getId());

					String priority = "";
					if (recommendation.getPriorityId().longValue() == 1) {
						priority = PriorityEnum.High.getName();
					} else if (recommendation.getPriorityId().longValue() == 2) {
						priority = PriorityEnum.Medium.getName();
					} else {
						priority = PriorityEnum.Low.getName();
					}

					byte[] userRecommendationfile = null;
					String fileName = null;

					if (recommendation.getFileUrl() != null) {
						userRecommendationfile = convertMultipartFileToBytes(recommendation.getFileUrl());
						fileName = recommendation.getReferenceId();
					}

					String agmEmail = userDepartment.get().getAgm().getEmail();
					String applicationOwnerEmail = userDepartment.get().getApplicationOwner().getEmail();
					String OemMail = user.get().getEmail();
					String[] ccEmails = {};
					String sendMail = "";

					String mailSubject = "";
					String mailHeading = "";

					if (status.equals(RecommendationStatusEnum.CREATED)) {

						mailSubject = "OEM Recommendation Request";
						mailHeading = "OEM Recommendation Request";
						sendMail = agmEmail;
						ccEmails = new String[] { applicationOwnerEmail };

					} else if (status.equals(RecommendationStatusEnum.APPROVED_BY_AGM)) {

						mailSubject = "OEM Recommendation Approved";
						mailHeading = "OEM Recommendation Approved By AGM";
						sendMail = OemMail;
						ccEmails = new String[] { applicationOwnerEmail };
					}

					String content = String.format("<div style='background-color: #f4f4f4; padding: 20px;'>"
							+ "<div style='max-width: 1600px; margin: 0 auto; background-color: #ffffff; padding: 25px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);'>"
							+ "<div style='background-image: url(https://1000logos.net/wp-content/uploads/2018/03/SBI-Logo.jpg);"
							+ "background-size: 150px; background-position: top right; background-repeat: no-repeat;  background-color: rgba(255, 255, 255, 01); height: auto;'>"
							+ "<h1 style='font-size: 24px; color: #333; font-weight: bold;'> %s </h1>" + "<br>"
							+ "<p style='font-size: 20px; color: #333; font-weight: bold;'>Dear User,</p>"
							+ "<p style='font-size: 16px; color: #333;'>We would like to bring to your attention a new recommendation with the following details:</p>"
							+ "<p style='font-size: 16px; color: #333;'><b> Reference Id : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #333;'><b> Recommendation Type : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #333;'><b> Priority Type : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #333;'><b> Descriptions : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #333;'><b> Department Name :</b> %s</p>"
							+ "<p style='font-size: 16px; color: #333;'><b> Component Name : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #333;'><b> Recommend Date : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #333;'><b> Expected Impact : </b>%s</p>" + "<br>"
							+ "<p style='font-size: 16px; color: #333;'>If you have any further questions or concerns, please feel free to contact us.</p>"
							+ "<p style='font-size: 16px; color: #333;'>Best regards,</p>"
							+ "<p style='font-size: 16px; color: #333;'>OEM Team</p>" + "</div>" + "</div>" + "</div>",
							mailHeading, recommendation.getReferenceId(),
							userRecommendationType.get().getName() != null ? userRecommendationType.get().getName()
									: "NA",
							priority != null ? priority : "NA",
							recommendation.getDescriptions() != null ? recommendation.getDescriptions() : "NA",
							userDepartment.get().getDepartment().getName() != null
									? userDepartment.get().getDepartment().getName()
									: "NA",
							userComponent.get().getName() != null ? userComponent.get().getName() : "NA",
							recommendation.getRecommendDate() != null ? formatDate(recommendation.getRecommendDate())
									: "NA",
							recommendation.getExpectedImpact() != null ? recommendation.getExpectedImpact() : "NA");

					emailService.sendMailAndFile(sendMail, ccEmails, mailSubject, content, userRecommendationfile,
							fileName);

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

	@Override
	public Response<?> sendMailRecommendationDeplyomentDetails(RecommendationDeplyomentDetails details,
			RecommendationStatusEnum status) {
		try {

			CompletableFuture.runAsync(() -> {

				try {

					Optional<Recommendation> userRecommendation = recommendationRepository
							.findByReferenceId(details.getRecommendRefId());

					Optional<DepartmentApprover> userDepartment = departmentApproverRepository
							.findAllByDepartmentId(userRecommendation.get().getDepartment().getId());

					String OemMail = userRecommendation.get().getCreatedBy().getEmail();

					String sendEmail = "";
					String mailSubject = "";
					String mailHeading = "";
					String[] ccEmails = { OemMail };

					if (status.equals(RecommendationStatusEnum.APPROVED_BY_APPOWNER)) {

						mailSubject = "Appication Owner Approval";
						mailHeading = "OEM Recommended Request Accepted";
						sendEmail = userDepartment.get().getAgm().getEmail();

					} else if (status.equals(RecommendationStatusEnum.UPDATE_DEPLOYMENT_DETAILS)) {

						mailSubject = "Update Deployment Details";
						mailHeading = "OEM Recommended Deployment Details";
						sendEmail = userDepartment.get().getAgm().getEmail();

					}

					String content = String.format("<div style='background-color: #f4f4f4; padding: 20px;'>"
							+ "<div style='max-width: 1600px; margin: 0 auto; background-color: #ffffff; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);'>"
							+ "<div style='background-image: url(https://1000logos.net/wp-content/uploads/2018/03/SBI-Logo.jpg);"
							+ "background-size: 150px; background-position: top right; background-repeat: no-repeat;  background-color: rgba(255, 255, 255, 01); height: auto;'>"
							+ "<h1 style='font-size: 24px; color: #333; font-weight: bold; '> %s</h1>" + "<br>"
							+ "<p style='font-size: 20px; color: #333; font-weight: bold;'>Dear User,</p>"
							+ "<p style='font-size: 16px; color: #333;'>We would like to bring to your attention a new Deplyoment Details:</p>"
							+ "<p style='font-size: 16px; color: #555;  '><b> Reference Id : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #555;  '><b> Development Start Date : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #555;  '><b> Development End Date : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #555;  '><b> Test Completion Date : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #555;  '><b> Deployment Date : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #555;  '><b> Impacted Department : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #555;  '><b> Global Support Number : </b>%s</p>"
							+ "<br>"
							+ "<p style='font-size: 16px; color: #333;'>If you have any further questions or concerns, please feel free to contact us.</p>"
							+ "<p style='font-size: 16px; color: #333;'>Best regards,</p>"
							+ "<p style='font-size: 16px; color: #333;'>OEM Team</p>" + "</div>" + "</div>" + "</div>",

							mailHeading, details.getRecommendRefId(),
							details.getDevelopmentStartDate() != null ? formatDate(details.getDevelopmentStartDate())
									: "NA",
							details.getDevelopementEndDate() != null ? formatDate(details.getDevelopementEndDate())
									: "NA",
							details.getTestCompletionDate() != null ? formatDate(details.getTestCompletionDate())
									: "NA",
							details.getDeploymentDate() != null ? formatDate(details.getDeploymentDate()) : "NA",
							details.getImpactedDepartment() != null ? details.getImpactedDepartment() : "NA",
							details.getGlobalSupportNumber() != null ? details.getGlobalSupportNumber() : "NA"

					);

					emailService.sendMail(sendEmail, ccEmails, mailSubject, content);

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

	@Override
	public Response<?> sendMailRecommendationMessages(RecommendationMessages messages,
			RecommendationStatusEnum status) {

		try {

			CompletableFuture.runAsync(() -> {

				try {

					Optional<Recommendation> userRecommendation = recommendationRepository
							.findByReferenceId(messages.getReferenceId());

					Optional<DepartmentApprover> userDepartment = departmentApproverRepository
							.findAllByDepartmentId(userRecommendation.get().getDepartment().getId());

					String sendEmail = "";
					String mailSubject = "";
					String mailHeading = "";
					String[] ccEmail = {};

					if (status.equals(RecommendationStatusEnum.REJECTED_BY_APPOWNER)) {

						mailSubject = "OEM Recommendation Rejected ";
						mailHeading = "OEM Recommendation Rejected by Application Owner";
						sendEmail = userDepartment.get().getAgm().getEmail();

					} else if (status.equals(RecommendationStatusEnum.REJECTED_BY_AGM)) {

						mailSubject = "OEM Recommendation Rejected ";
						mailHeading = "OEM Recommendation Rejected by AGM";
						sendEmail = userDepartment.get().getApplicationOwner().getEmail();
					} else if (status.equals(RecommendationStatusEnum.REVERTED_BY_AGM)) {

						mailSubject = "OEM Recommendation Reverted ";
						mailHeading = "OEM Recommendation Reverted by AGM";
						sendEmail = userDepartment.get().getApplicationOwner().getEmail();

					} else if (status.equals(RecommendationStatusEnum.RECCOMENDATION_REJECTED)) {

						mailSubject = "OEM Recommendation Rejected Completely";
						mailHeading = "OEM Recommendation Rejected by AGM && Application Owner";
						// OEM mail
						sendEmail = userRecommendation.get().getCreatedBy().getEmail();
					}

					String content = String.format("<div style='background-color: #f4f4f4; padding: 20px;'>"
							+ "<div style='max-width: 1600px; margin: 0 auto; background-color: #ffffff; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);'>"
							+ "<div style='background-image: url(https://1000logos.net/wp-content/uploads/2018/03/SBI-Logo.jpg);"
							+ "background-size: 150px; background-position: top right; background-repeat: no-repeat;  background-color: rgba(255, 255, 255, 01); height: auto;'>"
							+ "<h1 style='font-size: 24px; color: #333; font-weight: bold; '> %s </h1>" + "<br>"
							+ "<p style='font-size: 20px; color: #333; font-weight: bold;'>Dear User,</p>"
							+ "<p style='font-size: 16px; color: #333;'>We would like to bring to your attention a new Recommendation Messages of the Deplyoment Details:</p>"
							+ "<p style='font-size: 16px; color: #555;  '><b> Reference ID : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #555;  '><b> RejectionReason : </b>%s</p>"
							+ "<p style='font-size: 16px; color: #555;  '><b> AdditionalMessage : </b>%s</p>" + "<br>"
							+ "<p style='font-size: 16px; color: #333;'>If you have any further questions or concerns, please feel free to contact us.</p>"
							+ "<p style='font-size: 16px; color: #333;'>Best regards,</p>"
							+ "<p style='font-size: 16px; color: #333;'>OEM Team</p>" + "</div>" + "</div>" + "</div>",

							mailHeading, messages.getReferenceId() != null ? messages.getReferenceId() : "NA",
							messages.getRejectionReason() != null ? messages.getRejectionReason() : "NA",
							messages.getAdditionalMessage() != null ? messages.getAdditionalMessage() : "NA"

					);

					emailService.sendMail(sendEmail, ccEmail, mailSubject, content);

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

}
