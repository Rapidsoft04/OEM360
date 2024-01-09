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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.sbi.oem.dto.RecommendationDetailsRequestDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.enums.PriorityEnum;
import com.sbi.oem.enums.RecommendationStatusEnum;
import com.sbi.oem.enums.StatusEnum;
import com.sbi.oem.model.Component;
import com.sbi.oem.model.Department;
import com.sbi.oem.model.DepartmentApprover;
import com.sbi.oem.model.Recommendation;
import com.sbi.oem.model.RecommendationMessages;
import com.sbi.oem.model.RecommendationType;
import com.sbi.oem.model.User;
import com.sbi.oem.repository.ComponentRepository;
import com.sbi.oem.repository.DepartmentApproverRepository;
import com.sbi.oem.repository.RecommendationRepository;
import com.sbi.oem.repository.RecommendationTypeRepository;
import com.sbi.oem.repository.UserRepository;
import com.sbi.oem.service.EmailTemplateService;

@Service
public class EmailTemplateServiceImpl implements EmailTemplateService {

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

	@Autowired
	private JavaMailSender javaMailService;

	@Override
	public Response<?> sendMailRecommendation(Recommendation recommendation, RecommendationStatusEnum status) {

		try {
			MimeMessage mimeMsg = javaMailService.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, true);

			try {

				Optional<DepartmentApprover> userDepartment = departmentApproverRepository
						.findAllByDepartmentId(recommendation.getDepartment().getId());

				Optional<Component> userComponent = componentRepository.findById(recommendation.getComponent().getId());

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

				String RecommendationStatus = "";
				if (recommendation.getRecommendationStatus().getId() == 1) {
					RecommendationStatus = StatusEnum.OEM_recommendation.getName();
				} else if (recommendation.getRecommendationStatus().getId() == 2) {
					RecommendationStatus = StatusEnum.Review_process.getName();
				} else if (recommendation.getRecommendationStatus().getId() == 3) {
					RecommendationStatus = StatusEnum.Approved.getName();
				} else if (recommendation.getRecommendationStatus().getId() == 4) {
					RecommendationStatus = StatusEnum.Rejected.getName();
				} else if (recommendation.getRecommendationStatus().getId() == 5) {
					RecommendationStatus = StatusEnum.Department_implementation.getName();
				} else if (recommendation.getRecommendationStatus().getId() == 6) {
					RecommendationStatus = StatusEnum.UAT_testing.getName();
				} else if (recommendation.getRecommendationStatus().getId() == 7) {
					RecommendationStatus = StatusEnum.Released.getName();
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
				String userName = "";

				String mailSubject = "";
				String mailHeading = "";

				if (status.equals(RecommendationStatusEnum.CREATED)) {

					mailSubject = "OEM Recommendation Request";
					mailHeading = "OEM Recommendation Request";
					sendMail = agmEmail;
					userName = userDepartment.get().getAgm().getUserName();
					ccEmails = new String[] { applicationOwnerEmail };

				} else if (status.equals(RecommendationStatusEnum.APPROVED_BY_AGM)) {

					mailSubject = "OEM Recommendation Approved";
					mailHeading = "OEM Recommendation Approved By AGM";
					sendMail = OemMail;
					userName = user.get().getUserName();
					ccEmails = new String[] { applicationOwnerEmail };

				} else if (status.equals(RecommendationStatusEnum.RECOMMENDATION_STATUS_CHANGED)) {

					mailSubject = "Your Reference Id " + recommendation.getReferenceId() + "Status has been Changed";
					mailHeading = "OEM Recommendation Update";
					sendMail = agmEmail;
					userName = userDepartment.get().getAgm().getUserName();
					ccEmails = new String[] { agmEmail };
				} else if (status.equals(RecommendationStatusEnum.RECOMMENDATION_RELEASED)) {

					mailSubject = "Your Reference Id " + recommendation.getReferenceId() + "- OEM Recommendation "
							+ " has been Released";
					mailHeading = "OEM Recommendation Released";
					sendMail = OemMail;
					userName = user.get().getUserName();
					ccEmails = new String[] { agmEmail };

				}

				if (status.equals(RecommendationStatusEnum.RECOMMENDATION_STATUS_CHANGED)
						|| status.equals(RecommendationStatusEnum.RECOMMENDATION_RELEASED)) {

					String content = String.format(
							"<div style='background-color: #f4f4f4; padding: 20px; max-width: 100vw;'>"
									+ "<div style='max-width: 100vw; background-color: #ffffff; padding: 25px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); overflow: auto;'>"
									+ "<img style='max-height: 15vh; max-width: 15vw; background-repeat: no-repeat; float: right;' src='https://1000logos.net/wp-content/uploads/2018/03/SBI-Logo.jpg'/>"
									+ "<h1 class='header-title' style='font-size: 30px; margin: 0;'>%s</h1>"
									+ "<div style='clear: both;'></div>" + "<div>"
									+ String.format(
											"<p style='font-size: 20px; color: #333; font-weight: bold;'>Dear %s,</p>",
											userName)
									+ "<p style='font-size: 16px; color: #333;'>We would like to bring to your attention an updated recommendation with the following details:</p>"
									+ "<p style='font-size: 16px; color: #333;'><b>Reference Id :</b> %s</p>"
									+ "<p style='font-size: 16px; color: #333;'><b>Status :</b> %s</p>"
									+ "<p style='font-size: 16px; color: #333;'><b>Recommendation Type :</b> %s</p>"
									+ "<p style='font-size: 16px; color: #333;'><b>Priority Type :</b> %s</p>"
									+ "<p style='font-size: 16px; color: #333;'><b>Descriptions :</b> %s</p>"
									+ "<p style='font-size: 16px; color: #333;'><b>Department Name :</b> %s</p>"
									+ "<p style='font-size: 16px; color: #333;'><b>Component Name :</b> %s</p>"
									+ "<p style='font-size: 16px; color: #333;'><b>Recommend Date :</b> %s</p>"
									+ "<p style='font-size: 16px; color: #333;'><b>Expected Impact :</b> %s</p><br>"
									+ "<p style='font-size: 16px; color: #333;'>If you have any further questions or concerns, please feel free to contact us.</p>"
									+ "<p style='font-size: 16px; color: #333;'>Best regards,</p>" + "</div>" + "</div>"
									+ "</div>" + "<style>" + "@media screen and (max-width: 600px) {"
									+ ".header-image img {" + "margin-left: 10px; " + "}" + "}" + "</style>",

							mailHeading, recommendation.getReferenceId(),
							RecommendationStatus != null ? RecommendationStatus : "NA",
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

//						emailService.sendMailAndFile(sendMail, ccEmails, mailSubject, content, userRecommendationfile,
//								fileName);
					helper.setTo(sendMail);
					helper.setCc(ccEmails);
					helper.setSubject(mailSubject);
					helper.setText(content);
					Multipart multipart = new MimeMultipart();

					MimeBodyPart textPart = new MimeBodyPart();
					textPart.setText(content, "utf-8", "html");
					multipart.addBodyPart(textPart);

					if (userRecommendationfile != null && userRecommendationfile.length > 0) {
						MimeBodyPart attachmentPart = new MimeBodyPart();
						DataSource source = new ByteArrayDataSource(userRecommendationfile, "application/octet-stream");
						attachmentPart.setDataHandler(new DataHandler(source));
						attachmentPart.setFileName(fileName);
						multipart.addBodyPart(attachmentPart);
					}

					mimeMsg.setContent(multipart);
					EmailThread sendEmail = new EmailThread(javaMailService, mimeMsg);
					Thread parallelThread = new Thread(sendEmail);
					parallelThread.setPriority(Thread.MAX_PRIORITY);
					parallelThread.start();

				} else {

					String content = String.format(
							"<div style='background-color: #f4f4f4; padding: 20px; max-width: 100vw;'>"
									+ "<div style='max-width: 100vw; background-color: #ffffff; padding: 25px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); overflow: auto;'>"
									+ "<img style='max-height: 15vh; max-width: 15vw; background-repeat: no-repeat; float: right;' src='https://1000logos.net/wp-content/uploads/2018/03/SBI-Logo.jpg'/>"
									+ "<h1 class='header-title' style='font-size: 30px; margin: 0;'>%s</h1>"
									+ "<div style='clear: both;'></div>" + "<div>"
									+ String.format(
											"<p style='font-size: 20px; color: #333; font-weight: bold;'>Dear %s,</p>",
											userName)
									+ "<p style='font-size: 16px; color: #333;'>We would like to bring to your attention a New recommendation with the following details:</p>"
									+ "<p style='font-size: 16px; color: #333;'><b> Reference Id : </b>%s</p>"
									+ "<p style='font-size: 16px; color: #333;'><b> Recommendation Type : </b>%s</p>"
									+ "<p style='font-size: 16px; color: #333;'><b> Priority Type : </b>%s</p>"
									+ "<p style='font-size: 16px; color: #333;'><b> Descriptions : </b>%s</p>"
									+ "<p style='font-size: 16px; color: #333;'><b> Department Name :</b> %s</p>"
									+ "<p style='font-size: 16px; color: #333;'><b> Component Name : </b>%s</p>"
									+ "<p style='font-size: 16px; color: #333;'><b> Recommend Date : </b>%s</p>"
									+ "<p style='font-size: 16px; color: #333;'><b> Expected Impact : </b>%s</p>"
									+ "<br>"
									+ "<p style='font-size: 16px; color: #333;'>If you have any further questions or concerns, please feel free to contact us.</p>"
									+ "<p style='font-size: 16px; color: #333;'>Best regards,</p>" + "</div>" + "</div>"
									+ "</div>" + "<style>" + "@media screen and (max-width: 600px) {"
									+ ".header-image img {" + "margin-left: 10px; " + "}" + "}" + "</style>",
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

//						emailService.sendMailAndFile(sendMail, ccEmails, mailSubject, content, userRecommendationfile,
//								fileName);
					helper.setTo(sendMail);
					helper.setCc(ccEmails);
					helper.setSubject(mailSubject);
					helper.setText(content);
					Multipart multipart = new MimeMultipart();

					MimeBodyPart textPart = new MimeBodyPart();
					textPart.setText(content, "utf-8", "html");
					multipart.addBodyPart(textPart);

					if (userRecommendationfile != null && userRecommendationfile.length > 0) {
						MimeBodyPart attachmentPart = new MimeBodyPart();
						DataSource source = new ByteArrayDataSource(userRecommendationfile, "application/octet-stream");
						attachmentPart.setDataHandler(new DataHandler(source));
						attachmentPart.setFileName(fileName);
						multipart.addBodyPart(attachmentPart);
					}

					mimeMsg.setContent(multipart);
					EmailThread sendEmail = new EmailThread(javaMailService, mimeMsg);
					Thread parallelThread = new Thread(sendEmail);
					parallelThread.setPriority(Thread.MAX_PRIORITY);
					parallelThread.start();

				}

			} catch (MessagingException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {

			e.printStackTrace();
			return new Response<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to send email", null);
		}

		return new Response<>(HttpStatus.OK.value(), "Mail Send Successfully", null);
	}

	@Override
	public void sendMailRecommendationDeplyomentDetails(RecommendationDetailsRequestDto details,
			RecommendationStatusEnum status) {

		try {
			MimeMessage mimeMsg = javaMailService.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, true);

			try {

				Optional<Recommendation> userRecommendation = recommendationRepository
						.findByReferenceId(details.getRecommendRefId());

				Optional<DepartmentApprover> userDepartment = departmentApproverRepository
						.findAllByDepartmentId(userRecommendation.get().getDepartment().getId());

				String OemMail = userRecommendation.get().getCreatedBy().getEmail();

				String sendEmail = "";
				String mailSubject = "";
				String mailHeading = "";
				String userName = "";
				String[] ccEmails = { OemMail };

				if (status.equals(RecommendationStatusEnum.APPROVED_BY_APPOWNER)) {

					mailSubject = "Appication Owner Approval";
					mailHeading = "OEM Recommended Request Accepted";
					userName = userDepartment.get().getAgm().getUserName();
					sendEmail = userDepartment.get().getAgm().getEmail();

				} else if (status.equals(RecommendationStatusEnum.UPDATE_DEPLOYMENT_DETAILS)) {

					mailSubject = "Update Deployment Details";
					mailHeading = "OEM Recommended Deployment Details";
					userName = userDepartment.get().getAgm().getUserName();
					sendEmail = userDepartment.get().getAgm().getEmail();

				}

				String content = String.format(
						"<div style='background-color: #f4f4f4; padding: 20px; max-width: 100vw;'>"
								+ "<div style='max-width: 100vw; background-color: #ffffff; padding: 25px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); overflow: auto;'>"
								+ "<img style='max-height: 15vh; max-width: 15vw; background-repeat: no-repeat; float: right;' src='https://1000logos.net/wp-content/uploads/2018/03/SBI-Logo.jpg'/>"
								+ "<h1 class='header-title' style='font-size: 30px; margin: 0;'>%s</h1>"
								+ "<div style='clear: both;'></div>" + "<div>"
								+ String.format(
										"<p style='font-size: 20px; color: #333; font-weight: bold;'>Dear %s,</p>",
										userName)
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
								+ "<p style='font-size: 16px; color: #333;'>Best regards,</p>" + "</div>" + "</div>"
								+ "</div>" + "<style>" + "@media screen and (max-width: 600px) {"
								+ ".header-image img {" + "margin-left: 10px; " + "}" + "}" + "</style>",

						mailHeading, details.getRecommendRefId(),
						details.getDevelopmentStartDate() != null ? formatDate(details.getDevelopmentStartDate())
								: "NA",
						details.getDevelopementEndDate() != null ? formatDate(details.getDevelopementEndDate()) : "NA",
						details.getTestCompletionDate() != null ? formatDate(details.getTestCompletionDate()) : "NA",
						details.getDeploymentDate() != null ? formatDate(details.getDeploymentDate()) : "NA",
						details.getImpactedDepartment() != null ? details.getImpactedDepartment() : "NA",
						details.getGlobalSupportNumber() != null ? details.getGlobalSupportNumber() : "NA"

				);

//				emailService.sendMail(sendEmail, ccEmails, mailSubject, content);
				helper.setTo(sendEmail);
				helper.setCc(ccEmails);
				helper.setSubject(mailSubject);
				helper.setText(content, true);
				EmailThread sendMail = new EmailThread(javaMailService, mimeMsg);
				Thread parallelThread = new Thread(sendMail);
				parallelThread.setPriority(Thread.MAX_PRIORITY);
				parallelThread.start();

			} catch (MessagingException e) {
				e.printStackTrace();
			}

			List<Department> departmentList = details.getDepartmentList();

			StringJoiner joiner = new StringJoiner(", ");

			departmentList.forEach(department -> {
				joiner.add(department.getName());
			});

			String impactedDepartment = joiner.toString();

			departmentList.forEach(eachDept -> {

				Optional<DepartmentApprover> userDepartment2 = departmentApproverRepository
						.findAllByDepartmentId(eachDept.getId());

				User agm = userDepartment2.get().getAgm();

				String sendEmail = agm.getEmail();
				String userName = agm.getUserName();
				String[] ccEmails = { sendEmail };

				String mailSubject = "Your Reference Id " + details.getRecommendRefId() + "- "
						+ "Impacted Department(s)";
				String mailHeading = "Department Impacted";

				String content = String.format(
						"<div style='background-color: #f4f4f4; padding: 20px; max-width: 100vw;'>"
								+ "<div style='max-width: 100vw; background-color: #ffffff; padding: 25px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); overflow: auto;'>"
								+ "<img style='max-height: 15vh; max-width: 15vw; background-repeat: no-repeat; float: right;' src='https://1000logos.net/wp-content/uploads/2018/03/SBI-Logo.jpg'/>"
								+ "<h1 class='header-title' style='font-size: 30px; margin: 0;'>%s</h1>"
								+ "<div style='clear: both;'></div>" + "<div>"
								+ String.format(
										"<p style='font-size: 20px; color: #333; font-weight: bold;'>Dear %s,</p>",
										userName + " (" + eachDept.getName() + ")")
								+ "<p style='font-size: 16px; color: #333;'><b> With Reference Id:</b> %s</p>"
								+ "<p style='font-size: 16px; color: #333;'><b> These are the Impacted Department(s):</b> %s</p>"
								+ "<p style='font-size: 16px; color: #333;  '><b> Development Start Date : </b>%s</p>"
								+ "<p style='font-size: 16px; color: #333;  '><b> Development End Date : </b>%s</p>"
								+ "<p style='font-size: 16px; color: #333;  '><b> Test Completion Date : </b>%s</p>"
								+ "<p style='font-size: 16px; color: #333;  '><b> Deployment Date : </b>%s</p>"
								+ "<p style='font-size: 16px; color: #333;  '><b> Global Support Number : </b>%s</p>"
								+ "<br>"
								+ "<p style='font-size: 16px; color: #333;'>If you have any further questions or concerns, please feel free to contact us.</p>"
								+ "<p style='font-size: 16px; color: #333;'>Best regards,</p>" + "</div>" + "</div>"
								+ "</div>" + "<style>" + "@media screen and (max-width: 600px) {"
								+ ".header-image img {" + "margin-left: 10px; " + "}" + "}" + "</style>",
						mailHeading, details.getRecommendRefId(),
						impactedDepartment != null ? impactedDepartment : "NA",
						details.getDevelopmentStartDate() != null ? formatDate(details.getDevelopmentStartDate())
								: "NA",
						details.getDevelopementEndDate() != null ? formatDate(details.getDevelopementEndDate()) : "NA",
						details.getTestCompletionDate() != null ? formatDate(details.getTestCompletionDate()) : "NA",
						details.getDeploymentDate() != null ? formatDate(details.getDeploymentDate()) : "NA",

						details.getGlobalSupportNumber() != null ? details.getGlobalSupportNumber() : "NA"

				);

				try {

//					emailService.sendMail(sendEmail, ccEmails, mailSubject, content);
					helper.setTo(sendEmail);
					helper.setCc(ccEmails);
					helper.setSubject(mailSubject);
					helper.setText(content, true);
					EmailThread sendMail = new EmailThread(javaMailService, mimeMsg);
					Thread parallelThread = new Thread(sendMail);
					parallelThread.setPriority(Thread.MAX_PRIORITY);
					parallelThread.start();

				} catch (MessagingException e) {

					e.printStackTrace();
					System.out.println("mail not send");

				}

				System.out.println("mail send");

			});

		} catch (Exception e) {

			e.printStackTrace();
//			return new Response<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to send email", null);
		}

//		return new Response<>(HttpStatus.OK.value(), "Mail Send Successfully", null);

	}

	public static String formatDate(Date date) {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		Instant instant = date.toInstant();
		LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
		return localDate.format(dateFormatter);
	}

	@Override
	public void sendMailRecommendationMessages(RecommendationMessages messages, RecommendationStatusEnum status) {

		try {
			MimeMessage mimeMsg = javaMailService.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, true);

			try {

				Optional<Recommendation> userRecommendation = recommendationRepository
						.findByReferenceId(messages.getReferenceId());

				Optional<DepartmentApprover> userDepartment = departmentApproverRepository
						.findAllByDepartmentId(userRecommendation.get().getDepartment().getId());

				String sendEmail = "";
				String mailSubject = "";
				String mailHeading = "";
				String userName = "";
				String[] ccEmail = {};

				if (status.equals(RecommendationStatusEnum.REJECTED_BY_APPOWNER)) {

					mailSubject = "OEM Recommendation Rejected ";
					mailHeading = "OEM Recommendation Rejected by Application Owner";
					userName = userDepartment.get().getAgm().getUserName();
					sendEmail = userDepartment.get().getAgm().getEmail();

				} else if (status.equals(RecommendationStatusEnum.REJECTED_BY_AGM)) {

					mailSubject = "OEM Recommendation Rejected ";
					mailHeading = "OEM Recommendation Rejected by AGM";
					userName = userDepartment.get().getApplicationOwner().getUserName();
					sendEmail = userDepartment.get().getApplicationOwner().getEmail();

				} else if (status.equals(RecommendationStatusEnum.REVERTED_BY_AGM)) {

					mailSubject = "OEM Recommendation Reverted ";
					mailHeading = "OEM Recommendation Reverted by AGM";
					userName = userDepartment.get().getApplicationOwner().getUserName();
					sendEmail = userDepartment.get().getApplicationOwner().getEmail();

				} else if (status.equals(RecommendationStatusEnum.RECCOMENDATION_REJECTED)) {

					mailSubject = "OEM Recommendation Rejected Completely";
					mailHeading = "OEM Recommendation Rejected by AGM && Application Owner";
					// OEM mail
					sendEmail = userRecommendation.get().getCreatedBy().getEmail();
					userName = userRecommendation.get().getCreatedBy().getUserName();
				}

				String content = String.format(
						"<div style='background-color: #f4f4f4; padding: 20px; max-width: 100vw;'>"
								+ "<div style='max-width: 100vw; background-color: #ffffff; padding: 25px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); overflow: auto;'>"
								+ "<img style='max-height: 15vh; max-width: 15vw; background-repeat: no-repeat; float: right;' src='https://1000logos.net/wp-content/uploads/2018/03/SBI-Logo.jpg'/>"
								+ "<h1 class='header-title' style='font-size: 30px; margin: 0;'>%s</h1>"
								+ "<div style='clear: both;'></div>" + "<div>"
								+ String.format(
										"<p style='font-size: 20px; color: #333; font-weight: bold;'>Dear %s,</p>",
										userName)
								+ "<p style='font-size: 16px; color: #333;'>We would like to bring to your attention a new Recommendation Messages of the Deplyoment Details:</p>"
								+ "<p style='font-size: 16px; color: #555;  '><b> Reference ID : </b>%s</p>"
								+ "<p style='font-size: 16px; color: #555;  '><b> RejectionReason : </b>%s</p>"
								+ "<p style='font-size: 16px; color: #555;  '><b> AdditionalMessage : </b>%s</p>"
								+ "<br>"
								+ "<p style='font-size: 16px; color: #333;'>If you have any further questions or concerns, please feel free to contact us.</p>"
								+ "<p style='font-size: 16px; color: #333;'>Best regards,</p>" + "</div>" + "</div>"
								+ "</div>" + "<style>" + "@media screen and (max-width: 600px) {"
								+ ".header-image img {" + "margin-left: 10px; " + "}" + "}" + "</style>",

						mailHeading, messages.getReferenceId() != null ? messages.getReferenceId() : "NA",
						messages.getRejectionReason() != null ? messages.getRejectionReason() : "NA",
						messages.getAdditionalMessage() != null ? messages.getAdditionalMessage() : "NA"

				);

//					emailService.sendMail(sendEmail, ccEmail, mailSubject, content);

				helper.setTo(sendEmail);
				helper.setCc(ccEmail);
				helper.setSubject(mailSubject);
				helper.setText(content, true);
				EmailThread sendMail = new EmailThread(javaMailService, mimeMsg);
				Thread parallelThread = new Thread(sendMail);
				parallelThread.setPriority(Thread.MAX_PRIORITY);
				parallelThread.start();

			} catch (MessagingException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {

			e.printStackTrace();
		}

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
	public void sendMailBuldRecommendation(List<Recommendation> recommendationList) {

		try {
			MimeMessage mimeMsg = javaMailService.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, true, "UTF-8");

			try {

				Map<Long, List<Recommendation>> recommendationMap = recommendationList.stream()
						.collect(Collectors.groupingBy(recommendation -> recommendation.getDepartment().getId()));

				for (Map.Entry<Long, List<Recommendation>> entry : recommendationMap.entrySet()) {
					Long departmentId = entry.getKey();
					List<Recommendation> departmentRecommendations = entry.getValue();

					Optional<DepartmentApprover> userDepartment = departmentApproverRepository
							.findAllByDepartmentId(departmentId);

					String agmEmail = userDepartment.get().getAgm().getEmail();
					String applicationOwnerEmail = userDepartment.get().getApplicationOwner().getEmail();
					String userName = userDepartment.get().getAgm().getUserName();

					for (Recommendation recommendation : departmentRecommendations) {

						byte[] userRecommendationfile = null;
						String fileName = null;

						if (recommendation.getFileUrl() != null) {
							userRecommendationfile = convertMultipartFileToBytes(recommendation.getFileUrl());
							fileName = recommendation.getReferenceId();
						}

						String priority = "";

						if (recommendation.getPriorityId().longValue() == 1) {
							priority = PriorityEnum.High.getName();
						} else if (recommendation.getPriorityId().longValue() == 2) {
							priority = PriorityEnum.Medium.getName();
						} else {
							priority = PriorityEnum.Low.getName();
						}

						String mailSubject = "OEM Recommendation Request";
						String mailHeading = "OEM Recommendation Request";

						String content = String.format(
								"<div style='background-color: #f4f4f4; padding: 20px; max-width: 100vw;'>"
										+ "<div style='max-width: 100vw; background-color: #ffffff; padding: 25px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); overflow: auto;'>"
										+ "<img style='max-height: 15vh; max-width: 15vw; background-repeat: no-repeat; float: right;' src='https://1000logos.net/wp-content/uploads/2018/03/SBI-Logo.jpg'/>"
										+ "<h1 class='header-title' style='font-size: 30px; margin: 0;'>%s</h1>"
										+ "<div style='clear: both;'></div>" + "<div>"
										+ String.format(
												"<p style='font-size: 20px; color: #333; font-weight: bold;'>Dear %s,</p>",
												userName)
										+ "<p style='font-size: 16px; color: #333;'>We would like to bring to your attention a new recommendation with the following details:</p>"
										+ "<p style='font-size: 16px; color: #333;'><b> Reference Id : </b>%s</p>"
										+ "<p style='font-size: 16px; color: #333;'><b> Recommendation Type : </b>%s</p>"
										+ "<p style='font-size: 16px; color: #333;'><b> Priority Type : </b>%s</p>"
										+ "<p style='font-size: 16px; color: #333;'><b> Descriptions : </b>%s</p>"
										+ "<p style='font-size: 16px; color: #333;'><b> Department Name :</b> %s</p>"
										+ "<p style='font-size: 16px; color: #333;'><b> Component Name : </b>%s</p>"
										+ "<p style='font-size: 16px; color: #333;'><b> Recommend Date : </b>%s</p>"
										+ "<p style='font-size: 16px; color: #333;'><b> Expected Impact : </b>%s</p>"
										+ "<p style='font-size: 16px; color: #333;'>If you have any further questions or concerns, please feel free to contact us.</p>"
										+ "<p style='font-size: 16px; color: #333;'>Best regards,</p>" + "</div>"
										+ "</div>" + "</div>" + "<style>" + "@media screen and (max-width: 600px) {"
										+ ".header-image img {" + "margin-left: 10px; " + "}" + "}" + "</style>",
								mailHeading, recommendation.getReferenceId(),
								recommendation.getRecommendationType().getName() != null
										? recommendation.getRecommendationType().getName()
										: "NA",
								priority != null ? priority : "NA",
								recommendation.getDescriptions() != null ? recommendation.getDescriptions() : "NA",
								userDepartment.get().getDepartment().getName() != null
										? userDepartment.get().getDepartment().getName()
										: "NA",
								recommendation.getComponent().getName() != null
										? recommendation.getComponent().getName()
										: "NA",
								recommendation.getRecommendDate() != null
										? formatDate(recommendation.getRecommendDate())
										: "NA",
								recommendation.getExpectedImpact() != null ? recommendation.getExpectedImpact() : "NA");

						try {
//							emailService.sendMailAndFile(agmEmail, new String[] { applicationOwnerEmail }, mailSubject,
//									content, userRecommendationfile, fileName);

							helper.setTo(agmEmail);

							for (String ccRecipient : new String[] { applicationOwnerEmail }) {
								if (ccRecipient != null) {
									helper.addCc(ccRecipient);
								}
							}

							helper.setSubject(mailSubject);
							helper.setText(content, true);

							Multipart multipart = new MimeMultipart();

							MimeBodyPart textPart = new MimeBodyPart();
							textPart.setText(content, "utf-8", "html");
							multipart.addBodyPart(textPart);

							if (userRecommendationfile != null && userRecommendationfile.length > 0) {
								MimeBodyPart attachmentPart = new MimeBodyPart();
								DataSource source = new ByteArrayDataSource(userRecommendationfile,
										"application/octet-stream");
								attachmentPart.setDataHandler(new DataHandler(source));
								attachmentPart.setFileName(fileName);
								multipart.addBodyPart(attachmentPart);
							}

							mimeMsg.setContent(multipart);
							EmailThread sendMail = new EmailThread(javaMailService, mimeMsg);
							Thread parallelThread = new Thread(sendMail);
							parallelThread.setPriority(Thread.MAX_PRIORITY);
							parallelThread.start();

						} catch (MessagingException e) {

							System.out.println("Failed to send mail");

							e.printStackTrace();
						}
					}
				}

			} catch (Exception e) {

				e.printStackTrace();

			}

		} catch (Exception e) {

			e.printStackTrace();

		}

	}

	@Override
	public void sendAllMailForRecommendation(List<Recommendation> recommendationList, RecommendationStatusEnum status) {
		// TODO Auto-generated method stub
		try {
			MimeMessage mimeMsg = javaMailService.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, true, "UTF-8");

			try {
				for (Recommendation recommendation : recommendationList) {
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
					String userName = "";

					String mailSubject = "";
					String mailHeading = "";

					if (status.equals(RecommendationStatusEnum.CREATED)) {

						mailSubject = "OEM Recommendation Request";
						mailHeading = "OEM Recommendation Request";
						sendMail = agmEmail;
						userName = userDepartment.get().getAgm().getUserName();
						ccEmails = new String[] { applicationOwnerEmail };

					} else if (status.equals(RecommendationStatusEnum.APPROVED_BY_AGM)) {

						mailSubject = "OEM Recommendation Approved";
						mailHeading = "OEM Recommendation Approved By AGM";
						sendMail = OemMail;
						userName = user.get().getUserName();
						ccEmails = new String[] { applicationOwnerEmail };
					}

					String content = String.format(
							"<div style='background-color: #f4f4f4; padding: 20px; max-width: 100vw;'>"
									+ "<div style='max-width: 100vw; background-color: #ffffff; padding: 25px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); overflow: auto;'>"
									+ "<img style='max-height: 15vh; max-width: 15vw; background-repeat: no-repeat; float: right;' src='https://1000logos.net/wp-content/uploads/2018/03/SBI-Logo.jpg'/>"
									+ "<h1 class='header-title' style='font-size: 30px; margin: 0;'>%s</h1>"
									+ "<div style='clear: both;'></div>" + "<div>"
									+ String.format(
											"<p style='font-size: 20px; color: #333; font-weight: bold;'>Dear %s,</p>",
											userName)
									+ "<p style='font-size: 16px; color: #333;'>We would like to bring to your attention a new recommendation with the following details:</p>"
									+ "<p style='font-size: 16px; color: #333;'><b> Reference Id : </b>%s</p>"
									+ "<p style='font-size: 16px; color: #333;'><b> Recommendation Type : </b>%s</p>"
									+ "<p style='font-size: 16px; color: #333;'><b> Priority Type : </b>%s</p>"
									+ "<p style='font-size: 16px; color: #333;'><b> Descriptions : </b>%s</p>"
									+ "<p style='font-size: 16px; color: #333;'><b> Department Name :</b> %s</p>"
									+ "<p style='font-size: 16px; color: #333;'><b> Component Name : </b>%s</p>"
									+ "<p style='font-size: 16px; color: #333;'><b> Recommend Date : </b>%s</p>"
									+ "<p style='font-size: 16px; color: #333;'><b> Expected Impact : </b>%s</p>"
									+ "<br>"
									+ "<p style='font-size: 16px; color: #333;'>If you have any further questions or concerns, please feel free to contact us.</p>"
									+ "<p style='font-size: 16px; color: #333;'>Best regards,</p>" + "</div>" + "</div>"
									+ "</div>" + "<style>" + "@media screen and (max-width: 600px) {"
									+ ".header-image img {" + "margin-left: 10px; " + "}" + "}" + "</style>",
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

//					emailService.sendMailAndFile(sendMail, ccEmails, mailSubject, content, userRecommendationfile,
//							fileName);

					helper.setTo(sendMail);

					helper.setCc(ccEmails);

					helper.setSubject(mailSubject);
					helper.setText(content, true);

					Multipart multipart = new MimeMultipart();

					MimeBodyPart textPart = new MimeBodyPart();
					textPart.setText(content, "utf-8", "html");
					multipart.addBodyPart(textPart);

					if (userRecommendationfile != null && userRecommendationfile.length > 0) {
						MimeBodyPart attachmentPart = new MimeBodyPart();
						DataSource source = new ByteArrayDataSource(userRecommendationfile, "application/octet-stream");
						attachmentPart.setDataHandler(new DataHandler(source));
						attachmentPart.setFileName(fileName);
						multipart.addBodyPart(attachmentPart);
					}

					mimeMsg.setContent(multipart);
					EmailThread sendEmail = new EmailThread(javaMailService, mimeMsg);
					Thread parallelThread = new Thread(sendEmail);
					parallelThread.setPriority(Thread.MAX_PRIORITY);
					parallelThread.start();
				}

			} catch (MessagingException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

}
