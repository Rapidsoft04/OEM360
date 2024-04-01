package com.sbi.oem.serviceImpl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sbi.oem.dto.Response;
import com.sbi.oem.enums.UserType;
import com.sbi.oem.model.CredentialMaster;
import com.sbi.oem.model.RecommendationType;
import com.sbi.oem.repository.RecommendationTypeRepository;
import com.sbi.oem.security.JwtUserDetailsService;
import com.sbi.oem.service.RecommendationTypeService;

@Service
public class RecommendationTypeServiceImpl implements RecommendationTypeService {

	@Autowired
	private RecommendationTypeRepository recommendationTypeRepository;

	@Autowired
	private JwtUserDetailsService userDetailsService;

	@Override
	public Response<?> save(RecommendationType recommendationType) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.SUPER_ADMIN.name())) {
					if (!recommendationType.getName().isEmpty() && !recommendationType.getName().equals("")) {
						Optional<RecommendationType> recommendationTypeExist = recommendationTypeRepository
								.findRecommendationTypeByName(recommendationType.getName());

						if (!recommendationTypeExist.isPresent()) {
							recommendationType.setCompanyId(master.get().getUserId().getCompany().getId());
							recommendationType.setIsActive(true);
							recommendationTypeRepository.save(recommendationType);
							return new Response<>(HttpStatus.OK.value(), "success", null);
						} else {
							return new Response<>(HttpStatus.BAD_REQUEST.value(), "Recommendation Type already exist",
									null);
						}
					} else {
						return new Response<>(HttpStatus.BAD_REQUEST.value(),
								"Please provide a valid recommendation type", null);
					}
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access.", null);
				}
			} else {
				return new Response<>(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);
		}
	}

	@Override
	public Response<?> getAllByCompanyId() {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.SUPER_ADMIN.name())) {
					List<RecommendationType> list = recommendationTypeRepository
							.findAllByCompanyId(master.get().getUserId().getCompany().getId());
					return new Response<>(HttpStatus.OK.value(), "Recommendation Type List", list);
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access.", null);
				}
			} else {
				return new Response<>(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong.", null);
		}
	}

	@Override
	public Response<?> update(RecommendationType recommendationType) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.SUPER_ADMIN.name())) {
					if (recommendationType.getId() == null) {
						return new Response<>(HttpStatus.BAD_REQUEST.value(),
								"Please provide a valid recommendation type ID", null);
					} else if (recommendationType.getName().isEmpty() || recommendationType.getName().equals("")) {
						return new Response<>(HttpStatus.BAD_REQUEST.value(),
								"Please provide a valid recommendation type", null);
					}
					Optional<RecommendationType> recommendationTypeObject = recommendationTypeRepository
							.findById(recommendationType.getId());
					if (recommendationTypeObject == null) {
						return new Response<>(HttpStatus.BAD_REQUEST.value(), "Recommendation Type not found", null);
					}
					recommendationType.setCompanyId(recommendationTypeObject.get().getCompanyId());
					recommendationType.setCreatedAt(recommendationTypeObject.get().getCreatedAt());
					recommendationType.setUpdatedAt(new Date());
					recommendationTypeRepository.save(recommendationType);
					return new Response<>(HttpStatus.OK.value(), "Recommendation type updated successfully", null);
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access.", null);
				}
			} else {
				return new Response<>(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong.", null);
		}
	}
}
