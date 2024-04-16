package com.sbi.oem.serviceImpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sbi.oem.constant.Constant;
import com.sbi.oem.dto.RecommendationResponseDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.dto.SearchDto;
import com.sbi.oem.enums.PriorityEnum;
import com.sbi.oem.enums.StatusEnum;
import com.sbi.oem.enums.UserType;
import com.sbi.oem.model.CredentialMaster;
import com.sbi.oem.model.DepartmentApprover;
import com.sbi.oem.model.Recommendation;
import com.sbi.oem.model.RecommendationTrail;
import com.sbi.oem.repository.DepartmentApproverRepository;
import com.sbi.oem.repository.RecommendationRepository;
import com.sbi.oem.repository.RecommendationTrailRepository;
import com.sbi.oem.security.JwtUserDetailsService;
import com.sbi.oem.service.MisReportService;

@Service
public class MisReportServiceImpl implements MisReportService {

	@Autowired
	private RecommendationRepository recommendationRepository;

	@Autowired
	private JwtUserDetailsService userDetailsService;

	@Autowired
	private DepartmentApproverRepository departmentApproverRepository;

	@Autowired
	private RecommendationTrailRepository recommendationTrailRepository;

	@Override
	public Response<?> exportMisReportData(SearchDto searchDto) {
		// TODO Auto-generated method stub
		try {
//			String fromDate = "";
//			String toDate = "";
			String fromDate = searchDto.getFromDate();
			String toDate = searchDto.getToDate();
			String addedFromTime = "00:00:00";
			String addedToTime = "23:59:59";
			String value = searchDto.getDateFilterKey();
			if (value != null && !value.equals("")) {
				if (value.equals(Constant.TODAY)) {
					Date todayDate = new Date();
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
					String formattedDate = formatter.format(todayDate);
					fromDate = formattedDate + " " + addedFromTime;
					toDate = formattedDate + " " + addedToTime;
				} else if (value.equals(Constant.THIS_MONTH)) {
					Calendar calendar = Calendar.getInstance();
					calendar.set(Calendar.DAY_OF_MONTH, 1);
					Date startDate = calendar.getTime();
					calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
					Date endDate = calendar.getTime();
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					String formattedStartDate = dateFormat.format(startDate);
					String formattedEndDate = dateFormat.format(endDate);
					fromDate = formattedStartDate + " " + addedFromTime;
					toDate = formattedEndDate + " " + addedToTime;
				} else if (value.equals(Constant.THIS_WEEK)) {
					Calendar calendar = Calendar.getInstance();
					calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
					Date startDate = calendar.getTime();
					calendar.add(Calendar.DAY_OF_WEEK, 6);
					Date endDate = calendar.getTime();
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					String formattedStartDate = dateFormat.format(startDate);
					String formattedEndDate = dateFormat.format(endDate);
					fromDate = formattedStartDate + " " + addedFromTime;
					toDate = formattedEndDate + " " + addedToTime;
				} else if (value.equals(Constant.QUARTERLY)) {
					Calendar calendar = Calendar.getInstance();
					Date endDate = new Date();
					calendar.setTime(endDate);
					calendar.add(Calendar.MONTH, -3);
					Date startDate = calendar.getTime();
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					String formattedStartDate = dateFormat.format(startDate);
					String formattedEndDate = dateFormat.format(endDate);
					fromDate = formattedStartDate + " " + addedFromTime;
					toDate = formattedEndDate + " " + addedToTime;
				}
			} else if (fromDate != null && !fromDate.isEmpty()) {
				fromDate = fromDate + " " + addedFromTime;
			} else if (toDate != null && !toDate.isEmpty()) {
				toDate = toDate + " " + addedToTime;
			}
			if (fromDate == null || fromDate.isEmpty() || fromDate.trim().equals("")) {
				searchDto.setFromDate(null);
			}
			if (toDate == null || toDate.isEmpty() || toDate.trim().equals("")) {
				searchDto.setToDate(null);
			}

			searchDto.setFromDate(fromDate);
			searchDto.setToDate(toDate);
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			List<Recommendation> recommendationList = new ArrayList<>();
			if (master != null && master.isPresent()) {
				if ((master.get().getUserTypeId().name().equals(UserType.GM_IT_INFRA.name()))) {
					searchDto.setCreatedBy(null);
					recommendationList = recommendationRepository.findAllRecommendationsForGmBySearchDto(searchDto);
				} else if (master.get().getUserTypeId().name().equals(UserType.AGM.name())) {
					searchDto.setDepartmentId(master.get().getUserId().getDepartment() != null
							? master.get().getUserId().getDepartment().getId()
							: null);
					searchDto.setCreatedBy(null);
					recommendationList = recommendationRepository.findAllRecommendationsOemAndAgmBySearchDto(null,
							searchDto);
				} else if (master.get().getUserTypeId().name().equals(UserType.APPLICATION_OWNER.name())) {
					searchDto.setDepartmentId(master.get().getUserId().getDepartment() != null
							? master.get().getUserId().getDepartment().getId()
							: null);
					searchDto.setCreatedBy(master.get().getUserId().getId());
					recommendationList = recommendationRepository.findAllRecommendationsOemAndAgmBySearchDto(null,
							searchDto);
				} else if (master.get().getUserTypeId().name().equals(UserType.DGM.name())) {
					searchDto.setCreatedBy(null);
					List<DepartmentApprover> departmentApproverList = departmentApproverRepository
							.findByAgmIdOrDgmId(master.get().getUserId().getId());
					if (!departmentApproverList.isEmpty()) {
						List<Long> departmentIds = departmentApproverList.stream()
								.map(departmentApprover -> departmentApprover.getDepartment().getId())
								.collect(Collectors.toList());
						searchDto.setDepartmentIds(departmentIds);
					} else {
						searchDto.setDepartmentId(master.get().getUserId().getDepartment() != null
								? master.get().getUserId().getDepartment().getId()
								: null);
					}
					recommendationList = recommendationRepository.findAllRecommendationsOemAndAgmBySearchDto(null,
							searchDto);
				}
				List<RecommendationResponseDto> list = new ArrayList<>();
				for (Recommendation recommendation : recommendationList) {
					RecommendationResponseDto responseDto = recommendation.convertToDto();
					if (recommendation.getPriorityId().longValue() == PriorityEnum.High.getId().longValue()) {
						responseDto.setPriority(PriorityEnum.High.getName());
					}
					if (recommendation.getPriorityId().longValue() == PriorityEnum.Medium.getId().longValue()) {
						responseDto.setPriority(PriorityEnum.Medium.getName());
					}
					if (recommendation.getPriorityId().longValue() == PriorityEnum.Low.getId().longValue()) {
						responseDto.setPriority(PriorityEnum.Low.getName());
					}
					Optional<RecommendationTrail> trailObj = recommendationTrailRepository
							.findAllByReferenceIdAndStatusId(recommendation.getReferenceId(),
									StatusEnum.Released.getId());
					if (trailObj.isPresent()) {
						responseDto.setRecommendationReleasedDate(trailObj.get().getCreatedAt());
					}
					responseDto.setRecommendedBy(recommendation.getRecommendedBy());
					list.add(responseDto);
				}
				return new Response<>(HttpStatus.OK.value(), "Mis Report Data.", list);
			} else {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Invalid Credentials", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);
		}
	}

}
