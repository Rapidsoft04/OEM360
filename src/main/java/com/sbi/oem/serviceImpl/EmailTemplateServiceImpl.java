package com.sbi.oem.serviceImpl;

import java.util.Optional;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sbi.oem.dto.Response;
import com.sbi.oem.enums.PriorityEnum;
import com.sbi.oem.model.Component;
import com.sbi.oem.model.DepartmentApprover;
import com.sbi.oem.model.Recommendation;
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

	@Autowired
	private RecommendationService recommendationService;

	@Override
	public Response<?> sendMail(Recommendation recommendation) {

		try {

			System.out.println(recommendation.getComponent().getName()

					+ " " + recommendation.getDepartment().getName());

			Optional<DepartmentApprover> userDepartment = departmentApproverRepository
					.findAllByDepartmentId(recommendation.getDepartment().getId());
			Optional<Component> userComponent = componentRepository.findById(recommendation.getComponent().getId());
			Optional<RecommendationType> userRecommendationType = recommendationTypeRepository
					.findById(recommendation.getRecommendationType().getId());

			String agmEmail = userDepartment.get().getAgm().getEmail();
			String applicationOwnerEmail = userDepartment.get().getApplicationOwner().getEmail();
			String mailSubject = "Recommendation Approved";

			String content = String.format("<div style='background-color: #f4f4f4; padding: 20px;'>"
					+ "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);'>"
					+ "<h1 style='font-size: 24px; color: #333; font-weight: bold;'>OEM Recommendation Approval</h1>"
					+ "<p style='font-size: 16px; color: #555; font-weight: bold;'>Reference Id : %s</p>"
					+ "<p style='font-size: 16px; color: #555; font-weight: bold;'>Update Type : %s</p>"
					+ "<p style='font-size: 16px; color: #555; font-weight: bold;'>Priority Type : %s</p>"
					+ "<p style='font-size: 16px; color: #555; font-weight: bold;'>Descriptions : %s</p>"
					+ "<p style='font-size: 16px; color: #555; font-weight: bold;'>Department Name : %s</p>"
					+ "<p style='font-size: 16px; color: #555; font-weight: bold;'>Component Name : %s</p>"
					+ "<p style='font-size: 16px; color: #555; font-weight: bold;'>Recommend Date : %s</p>"
					+ "<p style='font-size: 16px; color: #555; font-weight: bold;'>Expected Impact : %s</p>"
					+ "<p style='font-size: 16px; color: #555; font-weight: bold;'>Url link: %s</p>" + "</div>"
					+ "</div>", recommendation.getReferenceId(), userRecommendationType.get().getName(),
					recommendation.getPriorityId(), recommendation.getDescriptions(),
					userDepartment.get().getDepartment().getName(), userComponent.get().getName(),
					recommendation.getCreatedAt().toLocaleString(),
					recommendation.getExpectedImpact() != null ? recommendation.getExpectedImpact() : "NA",
					recommendation.getFileUrl() != null ? recommendation.getFileUrl() : "NA");

			Thread emailThread = new Thread(() -> {
				try {

					emailService.sendMail(agmEmail, mailSubject, content);
					System.out.println("Mail sent to AGM successfully!!");

					emailService.sendMail(applicationOwnerEmail, mailSubject, content);
					System.out.println("Mail sent to Application Owner successfully!!");

				} catch (MessagingException e) {
					e.printStackTrace();
				}
			});

			emailThread.start();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return new Response<>(HttpStatus.OK.value(), "Mail Send Successfully", null);
	}

}
