package com.sbi.oem.serviceImpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sbi.oem.constant.Constant;
import com.sbi.oem.dto.DashboardResponseDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.enums.StatusEnum;
import com.sbi.oem.enums.UserType;
import com.sbi.oem.model.CredentialMaster;
import com.sbi.oem.model.DepartmentApprover;
import com.sbi.oem.model.Recommendation;
import com.sbi.oem.model.RecommendationDeplyomentDetails;
import com.sbi.oem.model.RecommendationTrail;
import com.sbi.oem.repository.DepartmentApproverRepository;
import com.sbi.oem.repository.RecommendationDeplyomentDetailsRepository;
import com.sbi.oem.repository.RecommendationRepository;
import com.sbi.oem.repository.RecommendationTrailRepository;
import com.sbi.oem.security.JwtUserDetailsService;
import com.sbi.oem.service.DashboardService;
import com.sbi.oem.util.DateUtil;

@Service
public class DashboardServiceImpl implements DashboardService {

	@Autowired
	private RecommendationRepository recommendationRepository;

	@Autowired
	private JwtUserDetailsService userDetailsService;

	@Autowired
	private DepartmentApproverRepository departmentApproverRepository;

	@Autowired
	private RecommendationDeplyomentDetailsRepository recommendationDeplyomentDetailsRepository;

	@Autowired
	private RecommendationTrailRepository recommendationTrailRepository;

	@Override
	public Response<?> getDashboardDetails(String value) {
		try {
			String fromDate = "";
			String toDate = "";
			String addedFromTime = "00:00:00";
			String addedToTime = "23:59:59";
			if (value.equals(Constant.TODAY)) {
				Date todayDate = new Date();
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				String formattedDate = formatter.format(todayDate);
				fromDate = formattedDate + " " + addedFromTime;
				toDate = formattedDate + " " + addedToTime;
			} else if (value.equals(Constant.YESTERDAY)) {
				Calendar today = Calendar.getInstance();
				Calendar yesterday = (Calendar) today.clone();
				yesterday.add(Calendar.DAY_OF_MONTH, -1);
				Date utilYesterday = yesterday.getTime();
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				String formattedDate = formatter.format(utilYesterday);
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
			} else if (value.equals(Constant.LAST_MONTH)) {
				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.DAY_OF_MONTH, 1);
				calendar.add(Calendar.MONTH, -1);
				Date lastMonthStartDate = calendar.getTime();
				calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
				Date lastMonthEndDate = calendar.getTime();
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				String formattedLastMonthStartDate = dateFormat.format(lastMonthStartDate);
				String formattedLastMonthEndDate = dateFormat.format(lastMonthEndDate);
				fromDate = formattedLastMonthStartDate + " " + addedFromTime;
				toDate = formattedLastMonthEndDate + " " + addedToTime;
			} else {
				Date todayDate = new Date();
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				String formattedDate = formatter.format(todayDate);
				fromDate = formattedDate + " " + addedFromTime;
				toDate = formattedDate + " " + addedToTime;
			}
			DashboardResponseDto dashboardResponse = new DashboardResponseDto();
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.AGM.name())
						|| master.get().getUserTypeId().name().equals(UserType.DGM.name())) {
					List<DepartmentApprover> departmentApprover = departmentApproverRepository
							.findByAgmIdOrDgmId(master.get().getUserId().getId());
					List<Recommendation> recommendationList = new ArrayList<>();
					if (!departmentApprover.isEmpty()) {
						if (!value.equals(Constant.TILL_TODAY)) {
							recommendationList = recommendationRepository.findByAgmIdAndUpdatedAtBetween(
									departmentApprover.get(0).getDepartment().getId(), fromDate, toDate);
						} else {
							recommendationList = recommendationRepository.findAllByDepartmentIdAndCreatedAtBetweenToday(
									departmentApprover.get(0).getDepartment().getId(), toDate);
						}
						if (recommendationList != null && recommendationList.size() > 0) {
							Integer totalRecommendation = recommendationList.size();
							dashboardResponse.setTotalRecommendation(totalRecommendation.longValue());
							Long pendingForApprovalCount = 0L;
							Long rejectedRecommendationCount = 0L;
							Long approvedRecommendationToBeImplementCount = 0L;
							Long releasedRecommendationCount = 0L;
							Long onTimeDoneRecommendationCount = 0L;
							Long delayRecommendationCount = 0L;
							Long approvedRecommendationNotYetReleasedCount = 0L;
							for (Recommendation recommendation : recommendationList) {
								if (recommendation.getRecommendationStatus().getId().longValue() < StatusEnum.Approved
										.getId().longValue()) {
									pendingForApprovalCount = pendingForApprovalCount + 1L;
								}
								if (recommendation.getRecommendationStatus().getId().longValue() == StatusEnum.Released
										.getId().longValue()) {
									releasedRecommendationCount = releasedRecommendationCount + 1L;
									Optional<RecommendationTrail> trailObj = recommendationTrailRepository
											.findAllByReferenceIdAndStatusId(recommendation.getReferenceId(),
													StatusEnum.Released.getId());
									Date rcmdDate = DateUtil.convertDateToNigh12AM(recommendation.getRecommendDate());

									if (trailObj.get().getCreatedAt().before(rcmdDate)) {
										onTimeDoneRecommendationCount = onTimeDoneRecommendationCount + 1L;
									} else {
										delayRecommendationCount = delayRecommendationCount + 1L;
									}
								}
								if (recommendation.getIsAgmApproved() != null
										&& recommendation.getIsAgmApproved().booleanValue() == true
										&& (recommendation.getRecommendationStatus().getId()
												.longValue() >= StatusEnum.Approved.getId().longValue()
												&& recommendation.getRecommendationStatus().getId()
														.longValue() < StatusEnum.Released.getId().longValue())) {
									approvedRecommendationNotYetReleasedCount = approvedRecommendationNotYetReleasedCount
											+ 1L;
								}
								if (recommendation.getRecommendationStatus().getId().longValue() == StatusEnum.Rejected
										.getId().longValue()) {
									rejectedRecommendationCount = rejectedRecommendationCount + 1L;
								}
								if (recommendation.getIsAgmApproved() != null
										&& recommendation.getIsAgmApproved().booleanValue() == true
										&& (recommendation.getRecommendationStatus().getId()
												.longValue() >= StatusEnum.Approved.getId().longValue()
												&& recommendation.getRecommendationStatus().getId()
														.longValue() < StatusEnum.Released.getId().longValue())) {
									approvedRecommendationToBeImplementCount = approvedRecommendationToBeImplementCount
											+ 1L;
								}

							}
							dashboardResponse.setReleasedRecommendations(releasedRecommendationCount);
							dashboardResponse.setPendingForApproval(pendingForApprovalCount);
							dashboardResponse
									.setApprovedRecommendationNotYetReleased(approvedRecommendationNotYetReleasedCount);
							dashboardResponse.setRejectedRecommendation(rejectedRecommendationCount);
							dashboardResponse
									.setApprovedRecommendationsToBeImplement(approvedRecommendationToBeImplementCount);
							dashboardResponse.setOnTimeDoneRecommendationCount(onTimeDoneRecommendationCount);
							dashboardResponse.setDelayRecommendationsCount(delayRecommendationCount);
						} else {
							dashboardResponse.setTotalRecommendation(0L);
							dashboardResponse.setReleasedRecommendations(0L);
							dashboardResponse.setPendingForApproval(0L);
							dashboardResponse.setApprovedRecommendationNotYetReleased(0L);
							dashboardResponse.setRejectedRecommendation(0L);
							dashboardResponse.setApprovedRecommendationsToBeImplement(0L);
							dashboardResponse.setOnTimeDoneRecommendationCount(0L);
							dashboardResponse.setDelayRecommendationsCount(0L);
						}
						return new Response<>(HttpStatus.OK.value(), "Dashboard Details.", dashboardResponse);
					} else {

						return new Response<>(HttpStatus.OK.value(), "Dashboard response.", dashboardResponse);
					}
				} else if (master.get().getUserTypeId().name().equals(UserType.GM_IT_INFRA.name())) {
					List<Recommendation> recommendationList = new ArrayList<>();
					if (!value.equals(Constant.TILL_TODAY)) {
						recommendationList = recommendationRepository.getAllDataForGMAndUpdatedAtBetween(fromDate,
								toDate);
					} else {
						recommendationList = recommendationRepository.getAllDataForGMAndCreatedAtBetweenToday(toDate);
					}
					if (recommendationList != null && recommendationList.size() > 0) {
						Integer totalRecommendation = recommendationList.size();
						dashboardResponse.setTotalRecommendation(totalRecommendation.longValue());
						Long pendingForApprovalCount = 0L;
						Long rejectedRecommendationCount = 0L;
						Long approvedRecommendationToBeImplementCount = 0L;
						Long releasedRecommendationCount = 0L;
						Long onTimeDoneRecommendationCount = 0L;
						Long delayRecommendationCount = 0L;
						Long approvedRecommendationNotYetReleasedCount = 0L;
						for (Recommendation recommendation : recommendationList) {
							Date rcmdDate = DateUtil.convertDateToNigh12AM(recommendation.getRecommendDate());
							if (recommendation.getRecommendationStatus().getId().longValue() < StatusEnum.Approved
									.getId().longValue()) {
								pendingForApprovalCount = pendingForApprovalCount + 1L;
							}
							if (recommendation.getRecommendationStatus().getId().longValue() == StatusEnum.Released
									.getId().longValue()) {
								releasedRecommendationCount = releasedRecommendationCount + 1L;
								Optional<RecommendationTrail> trailObj = recommendationTrailRepository
										.findAllByReferenceIdAndStatusId(recommendation.getReferenceId(),
												StatusEnum.Released.getId());
								if (trailObj.isPresent()) {
									if (trailObj.get().getCreatedAt().before(rcmdDate)) {
										onTimeDoneRecommendationCount = onTimeDoneRecommendationCount + 1L;
									} else {
										delayRecommendationCount = delayRecommendationCount + 1L;
									}
								}
							}
							if (recommendation.getIsAgmApproved() != null
									&& recommendation.getIsAgmApproved().booleanValue() == true
									&& (recommendation.getRecommendationStatus().getId()
											.longValue() >= StatusEnum.Approved.getId().longValue()
											&& recommendation.getRecommendationStatus().getId()
													.longValue() < StatusEnum.Released.getId().longValue())) {
								approvedRecommendationNotYetReleasedCount = approvedRecommendationNotYetReleasedCount
										+ 1L;
							}
							if (recommendation.getRecommendationStatus().getId().longValue() == StatusEnum.Rejected
									.getId().longValue()) {
								rejectedRecommendationCount = rejectedRecommendationCount + 1L;
							}
							if (recommendation.getIsAgmApproved() != null
									&& recommendation.getIsAgmApproved().booleanValue() == true
									&& (recommendation.getRecommendationStatus().getId()
											.longValue() >= StatusEnum.Approved.getId().longValue()
											&& recommendation.getRecommendationStatus().getId()
													.longValue() < StatusEnum.Released.getId().longValue())) {
								approvedRecommendationToBeImplementCount = approvedRecommendationToBeImplementCount
										+ 1L;
							}

						}
						dashboardResponse.setReleasedRecommendations(releasedRecommendationCount);
						dashboardResponse.setPendingForApproval(pendingForApprovalCount);
						dashboardResponse
								.setApprovedRecommendationNotYetReleased(approvedRecommendationNotYetReleasedCount);
						dashboardResponse.setRejectedRecommendation(rejectedRecommendationCount);
						dashboardResponse
								.setApprovedRecommendationsToBeImplement(approvedRecommendationToBeImplementCount);
						dashboardResponse.setOnTimeDoneRecommendationCount(onTimeDoneRecommendationCount);
						dashboardResponse.setDelayRecommendationsCount(delayRecommendationCount);
						return new Response<>(HttpStatus.OK.value(), "Dashboard Details.", dashboardResponse);
					} else {
						return new Response<>(HttpStatus.OK.value(), "Dashboard response.", dashboardResponse);
					}

				} else {
					return new Response<>(HttpStatus.OK.value(), "Dashboard response.", dashboardResponse);
				}
			} else {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access.", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong.", null);
		}
	}

}
