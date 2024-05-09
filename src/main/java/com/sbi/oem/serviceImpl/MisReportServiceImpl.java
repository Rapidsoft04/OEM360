package com.sbi.oem.serviceImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.IndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import com.sbi.oem.model.RecommendationDeplyomentDetails;
import com.sbi.oem.model.RecommendationTrail;
import com.sbi.oem.repository.DepartmentApproverRepository;
import com.sbi.oem.repository.RecommendationDeplyomentDetailsRepository;
import com.sbi.oem.repository.RecommendationRepository;
import com.sbi.oem.repository.RecommendationTrailRepository;
import com.sbi.oem.security.JwtUserDetailsService;
import com.sbi.oem.service.MisReportService;
import com.sbi.oem.util.DateUtil;
import org.apache.poi.ss.usermodel.*;

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

	@Autowired
	private RecommendationDeplyomentDetailsRepository deplyomentDetailsRepository;

	@Override
	public Response<?> exportMisReportData(SearchDto searchDto) {
		try {
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
				} else if (value.equals(Constant.CUSTOM)) {
					if (fromDate != null && !fromDate.isEmpty() && !fromDate.equals("")) {
						fromDate = fromDate + " " + addedFromTime;
					}
					if (toDate != null && !toDate.isEmpty() && !fromDate.equals("")) {
						toDate = toDate + " " + addedToTime;
					}
				}
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
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "Unauthorized", null);
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
					Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
							.findByRecommendRefId(recommendation.getReferenceId());
					if (deploymentDetails.isPresent()) {
						responseDto.setImpactedDepartment(deploymentDetails.get().getImpactedDepartment());
					}
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

	@Override
	public void exportMisReportDataV2(SearchDto searchDto, HttpServletResponse response) {
		try {
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
				} else if (value.equals(Constant.CUSTOM)) {
					if (fromDate != null && !fromDate.isEmpty() && !fromDate.equals("")) {
						fromDate = fromDate + " " + addedFromTime;
					}
					if (toDate != null && !toDate.isEmpty() && !fromDate.equals("")) {
						toDate = toDate + " " + addedToTime;
					}
				}
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
				XSSFWorkbook workbook = new XSSFWorkbook();
				XSSFSheet sheet = workbook.createSheet("Summary");
				if (master.get().getUserTypeId().name().equals(UserType.GM_IT_INFRA.name())) {
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
//				else {
//					return new Response<>(HttpStatus.BAD_REQUEST.value(), "Unauthorized", null);
//				}
				List<RecommendationResponseDto> list = new ArrayList<>();
				List<RecommendationResponseDto> pendingApprovalList = new ArrayList<>();
				List<RecommendationResponseDto> completedRecommendation = new ArrayList<>();
				List<RecommendationResponseDto> onTimeCompletedRecommendation = new ArrayList<>();
				List<RecommendationResponseDto> delayCompletedRecommendation = new ArrayList<>();
				List<RecommendationResponseDto> inProgressRecommendation = new ArrayList<>();
				List<RecommendationResponseDto> rejectedRecommendation = new ArrayList<>();

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
					Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
							.findByRecommendRefId(recommendation.getReferenceId());
					if (deploymentDetails.isPresent()) {
						responseDto.setImpactedDepartment(deploymentDetails.get().getImpactedDepartment());
						responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
					}
					if (trailObj.isPresent()) {
						responseDto.setRecommendationReleasedDate(trailObj.get().getCreatedAt());
					}
					if (responseDto.getStatus().getId().longValue() < StatusEnum.Approved.getId().longValue()) {
						pendingApprovalList.add(responseDto);
					}
					if (responseDto.getStatus().getId().longValue() == StatusEnum.Released.getId().longValue()) {
						completedRecommendation.add(responseDto);
					}
					if (responseDto.getStatus().getId().longValue() == StatusEnum.Released.getId().longValue()) {
						Date rcmdDate = DateUtil.convertDateToNigh12AM(recommendation.getRecommendDate());
//						System.out.println(recommendation.getReferenceId());
						if (trailObj.get().getCreatedAt().before(rcmdDate)) {
							onTimeCompletedRecommendation.add(responseDto);
						} else {
							delayCompletedRecommendation.add(responseDto);
						}
					}
					if (responseDto.getStatus().getId().longValue() == StatusEnum.Rejected.getId().longValue()) {
						rejectedRecommendation.add(responseDto);
					}
					if (responseDto.getStatus().getId() >= StatusEnum.Approved.getId()
							&& responseDto.getStatus().getId() < StatusEnum.Released.getId()
							&& responseDto.getStatus().getId() != StatusEnum.Rejected.getId()) {
						inProgressRecommendation.add(responseDto);
					}
					responseDto.setRecommendedBy(recommendation.getRecommendedBy());
					Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
							.findAllByDepartmentId(recommendation.getDepartment().getId());
					if (departmentApprover != null && departmentApprover.isPresent()) {
						responseDto.setApprover(departmentApprover.get().getAgm());
					}
					list.add(responseDto);
				}
				Set<String> departmentName = list.stream().filter(e -> e.getDepartment() != null)
						.map(e -> e.getDepartment().getName()).collect(Collectors.toSet());
//				String filePath = "src/main/resources/output.xlsx";
				if (list != null && list.size() > 0) {
					int rowNum = 0;
					Row row = sheet.createRow(rowNum);
					Cell cell = row.createCell(0);
					cell.setCellValue("Consolidated - All Departments");
//					CellStyle style = workbook.createCellStyle();
//					style.setFont(getHeadingFont(workbook));
//					style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
//					style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
					cell.setCellStyle(getGreyBackgroundStyle(workbook));
					rowNum = rowNum + 1;
					for (int i = 1; i <= 7; i++) {
						row = sheet.createRow(rowNum);
						if (i == 1) {
							Cell cells = row.createCell(0);
							cells.setCellValue("Total Recommendations");
							Cell valueCell = row.createCell(1);
							valueCell.setCellValue(list.size());
							cells.setCellStyle(getBlueBackgroundStyle(workbook));
							rowNum = rowNum + 1;
						} else if (i == 2) {
							Cell cells = row.createCell(0);
							cells.setCellValue("Pending for Approval");
							Cell valueCell = row.createCell(1);
							valueCell.setCellValue(pendingApprovalList.size());
							cells.setCellStyle(getPinkBackgroundStyle(workbook));
							rowNum = rowNum + 1;
						} else if (i == 3) {
							Cell cells = row.createCell(0);
							cells.setCellValue("Total Completed");
							Cell valueCell = row.createCell(1);
							valueCell.setCellValue(completedRecommendation.size());
							cells.setCellStyle(getGreenBackgroundStyle(workbook));
							rowNum = rowNum + 1;
						} else if (i == 4) {
							Cell cells = row.createCell(0);
							cells.setCellValue("Completed - On Time");
							Cell valueCell = row.createCell(1);
							valueCell.setCellValue(onTimeCompletedRecommendation.size());
							cells.setCellStyle(getLightGreenBackgroundStyle(workbook));
							rowNum = rowNum + 1;
						} else if (i == 5) {
							Cell cells = row.createCell(0);
							cells.setCellValue("Completed - With Delay");
							Cell valueCell = row.createCell(1);
							valueCell.setCellValue(delayCompletedRecommendation.size());
							cells.setCellStyle(getLightGreenBackgroundStyle(workbook));
							rowNum = rowNum + 1;
						} else if (i == 6) {
							Cell cells = row.createCell(0);
							cells.setCellValue("In Progress");
							Cell valueCell = row.createCell(1);
							valueCell.setCellValue(inProgressRecommendation.size());
							cells.setCellStyle(getOrangeBackgroundStyle(workbook));
							rowNum = rowNum + 1;
						} else if (i == 7) {
							Cell cells = row.createCell(0);
							cells.setCellValue("Rejected");
							Cell valueCell = row.createCell(1);
							valueCell.setCellValue(rejectedRecommendation.size());
							cells.setCellStyle(getOrangeBackgroundStyle(workbook));
							rowNum = rowNum + 1;
						}
					}
					rowNum = rowNum + 2;
					for (String str : departmentName) {
						row = sheet.createRow(rowNum);
						Cell cell1 = row.createCell(0);
						cell1.setCellValue(str);
//						style.setFont(getHeadingFont(workbook));
//						style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
//						style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						cell1.setCellStyle(getYellowBackgroundCellStyle(workbook));
						rowNum = rowNum + 1;
						List<RecommendationResponseDto> pendingApprovalListForThisDept = new ArrayList<>();
						List<RecommendationResponseDto> completedRecommendationForThisDept = new ArrayList<>();
						List<RecommendationResponseDto> onTimeCompletedRecommendationForThisDept = new ArrayList<>();
						List<RecommendationResponseDto> delayCompletedRecommendationForThisDept = new ArrayList<>();
						List<RecommendationResponseDto> inProgressRecommendationForThisDept = new ArrayList<>();
						List<RecommendationResponseDto> rejectedRecommendationForThisDept = new ArrayList<>();
						List<RecommendationResponseDto> totalRecommendationForThisDept = new ArrayList<>();
						for (RecommendationResponseDto dto : list) {
							if (dto.getDepartment().getName().equals(str)) {
								totalRecommendationForThisDept.add(dto);
							}
							if (dto.getDepartment().getName().equals(str)
									&& dto.getStatus().getId().longValue() < StatusEnum.Approved.getId().longValue()) {
								pendingApprovalListForThisDept.add(dto);
							}
							if (dto.getDepartment().getName().equals(str)
									&& dto.getStatus().getId().longValue() == StatusEnum.Released.getId().longValue()) {
								completedRecommendationForThisDept.add(dto);
							}
							if (dto.getDepartment().getName().equals(str)
									&& dto.getStatus().getId().longValue() == StatusEnum.Released.getId().longValue()) {
								Date rcmdDate = DateUtil.convertDateToNigh12AM(dto.getRecommendDate());
//									System.out.println(recommendation.getReferenceId());
								if (dto.getCreatedAt().before(rcmdDate)) {
									onTimeCompletedRecommendationForThisDept.add(dto);
								} else {
									delayCompletedRecommendationForThisDept.add(dto);
								}
							}
							if (dto.getDepartment().getName().equals(str)
									&& dto.getStatus().getId().longValue() == StatusEnum.Rejected.getId().longValue()) {
								rejectedRecommendationForThisDept.add(dto);
							}
							if (dto.getDepartment().getName().equals(str)
									&& dto.getStatus().getId().longValue() >= StatusEnum.Approved.getId().longValue()
									&& dto.getStatus().getId().longValue() < StatusEnum.Released.getId().longValue()
									&& dto.getStatus().getId() != StatusEnum.Rejected.getId()) {
								inProgressRecommendationForThisDept.add(dto);
							}
						}
						for (int i = 1; i <= 7; i++) {
							row = sheet.createRow(rowNum);
							if (i == 1) {
								Cell cells = row.createCell(0);
								cells.setCellValue("Total Recommendations");
								Cell valueCell = row.createCell(1);
								valueCell.setCellValue(totalRecommendationForThisDept.size());
								cells.setCellStyle(getBlueBackgroundStyle(workbook));
								rowNum = rowNum + 1;
							} else if (i == 2) {
								Cell cells = row.createCell(0);
								cells.setCellValue("Pending for Approval");
								Cell valueCell = row.createCell(1);
								valueCell.setCellValue(pendingApprovalListForThisDept.size());
								cells.setCellStyle(getPinkBackgroundStyle(workbook));
								rowNum = rowNum + 1;
							} else if (i == 3) {
								Cell cells = row.createCell(0);
								cells.setCellValue("Total Completed");
								Cell valueCell = row.createCell(1);
								valueCell.setCellValue(completedRecommendationForThisDept.size());
								cells.setCellStyle(getGreenBackgroundStyle(workbook));
								rowNum = rowNum + 1;
							} else if (i == 4) {
								Cell cells = row.createCell(0);
								cells.setCellValue("Completed - On Time");
								Cell valueCell = row.createCell(1);
								valueCell.setCellValue(onTimeCompletedRecommendationForThisDept.size());
								cells.setCellStyle(getLightGreenBackgroundStyle(workbook));
								rowNum = rowNum + 1;
							} else if (i == 5) {
								Cell cells = row.createCell(0);
								cells.setCellValue("Completed - With Delay");
								Cell valueCell = row.createCell(1);
								valueCell.setCellValue(delayCompletedRecommendationForThisDept.size());
								cells.setCellStyle(getLightGreenBackgroundStyle(workbook));
								rowNum = rowNum + 1;
							} else if (i == 6) {
								Cell cells = row.createCell(0);
								cells.setCellValue("In Progress");
								Cell valueCell = row.createCell(1);
								valueCell.setCellValue(inProgressRecommendationForThisDept.size());
								cells.setCellStyle(getOrangeBackgroundStyle(workbook));
								rowNum = rowNum + 1;
							} else if (i == 7) {
								Cell cells = row.createCell(0);
								cells.setCellValue("Rejected");
								Cell valueCell = row.createCell(1);
								valueCell.setCellValue(rejectedRecommendationForThisDept.size());
								cells.setCellStyle(getRedBackgroundStyle(workbook));
								rowNum = rowNum + 1;
							}
						}
						rowNum = rowNum + 2;

					}
					for (int i = 1; i <= 4; i++) {
						Sheet sheet1 = null;
						if (i == 1) {
							rowNum = 0;
							sheet1 = workbook.createSheet("Completed");
							row = sheet1.createRow(rowNum);
							cell = row.createCell(0);
//							style.setFont(getTextFont(workbook));
//							style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
//							style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							cell.setCellStyle(getGreenBackgroundStyle(workbook));
							cell.setCellValue("Completed");
							cell = row.createCell(1);
//							style.setFont(getTextFont(workbook));
							cell.setCellStyle(getGreenBackgroundStyle(workbook));
//							cell.setCellStyle(style);
							cell.setCellValue(completedRecommendation.size());
							rowNum = rowNum + 1;
						}
						if (i == 2) {

							rowNum = 0;
							sheet1 = workbook.createSheet("Approval Pending");
							row = sheet1.createRow(rowNum);
							cell = row.createCell(0);
//							style.setFont(getTextFont(workbook));
//							style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
//							style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							cell.setCellStyle(getPinkBackgroundStyle(workbook));
							cell.setCellValue("Pending for Approval");
							cell = row.createCell(1);
//							style.setFont(getTextFont(workbook));
//							cell.setCellStyle(style);
							cell.setCellValue(pendingApprovalList.size());
							rowNum = rowNum + 1;
						}
						if (i == 3) {
							rowNum = 0;
							sheet1 = workbook.createSheet("Implementation");
							row = sheet1.createRow(rowNum);
							cell = row.createCell(0);
//							style.setFont(getTextFont(workbook));
//							style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
//							style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

							cell.setCellStyle(getOrangeBackgroundStyle(workbook));
							cell.setCellValue("Implementation");
							cell = row.createCell(1);
//							style.setFont(getTextFont(workbook));
//							cell.setCellStyle(style);
							cell.setCellValue(inProgressRecommendation.size());
							rowNum = rowNum + 1;
						}
						if (i == 4) {

							rowNum = 0;
							sheet1 = workbook.createSheet("Rejected");
							row = sheet1.createRow(rowNum);
							cell = row.createCell(0);
//							style.setFont(getTextFont(workbook));
//							style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
//							style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							cell.setCellStyle(getYellowBackgroundCellStyle(workbook));
							cell.setCellValue("Rejected");
							cell = row.createCell(1);
//							style.setFont(getTextFont(workbook));
//							cell.setCellStyle(style);
							cell.setCellValue(rejectedRecommendation.size());
							rowNum = rowNum + 1;
						}
						for (String str : departmentName) {
							List<RecommendationResponseDto> completedRecommendationForThisDept = new ArrayList<>();
							if (i == 1) {
								completedRecommendationForThisDept = list.stream()
										.filter(e -> e.getDepartment().getName().equals(str) && e.getStatus().getId()
												.longValue() == StatusEnum.Released.getId().longValue())
										.collect(Collectors.toList());
							}
							if (i == 2) {
								completedRecommendationForThisDept = list.stream()
										.filter(e -> e.getDepartment().getName().equals(str) && e.getStatus().getId()
												.longValue() < StatusEnum.Approved.getId().longValue())
										.collect(Collectors.toList());
							}
							if (i == 3) {
								completedRecommendationForThisDept = list.stream().filter(e -> e.getDepartment()
										.getName().equals(str)
										&& e.getStatus().getId().longValue() >= StatusEnum.Approved.getId().longValue()
										&& e.getStatus().getId().longValue() < StatusEnum.Released.getId().longValue()
										&& e.getStatus().getId() != StatusEnum.Rejected.getId())
										.collect(Collectors.toList());
							}
							if (i == 4) {
								completedRecommendationForThisDept = list.stream()
										.filter(e -> e.getDepartment().getName().equals(str) && e.getStatus().getId()
												.longValue() == StatusEnum.Rejected.getId().longValue())
										.collect(Collectors.toList());
							}
							row = sheet1.createRow(rowNum);
							if (completedRecommendationForThisDept.size() > 0) {
								cell = row.createCell(0);
								cell.setCellValue(str);
								cell.setCellStyle(getGreenBackgroundStyle(workbook));
								cell = row.createCell(1);
								cell.setCellValue(completedRecommendationForThisDept.size());
								rowNum = rowNum + 1;
							}

							if (completedRecommendationForThisDept != null
									&& completedRecommendationForThisDept.size() > 0) {
								row = sheet1.createRow(rowNum);
								for (int k = 0; k < 16; k++) {
									switch (k) {
									case 0: {

										cell = row.createCell(k);
										cell.setCellValue("Reference ID");
//										style.setFont(getHeadingFont(workbook));
										cell.setCellStyle(getGoldenBackgroundStyle(workbook));
										break;
									}
									case 1: {
										cell = row.createCell(k);
//										style.setFont(getHeadingFont(workbook));
										cell.setCellStyle(getGoldenBackgroundStyle(workbook));
										cell.setCellValue("Created on");
										break;
									}
									case 2: {
										cell = row.createCell(k);
//										style.setFont(getHeadingFont(workbook));
										cell.setCellStyle(getGoldenBackgroundStyle(workbook));
										cell.setCellValue("Type");
										break;
									}
									case 3: {
										cell = row.createCell(k);
//										style.setFont(getHeadingFont(workbook));
										cell.setCellStyle(getGoldenBackgroundStyle(workbook));
										cell.setCellValue("Priority");
										break;
									}
									case 4: {
										cell = row.createCell(k);
//										style.setFont(getHeadingFont(workbook));
										cell.setCellStyle(getGoldenBackgroundStyle(workbook));
										cell.setCellValue("Recommended end date");
										break;
									}
									case 5: {
										cell = row.createCell(k);
//										style.setFont(getHeadingFont(workbook));
										cell.setCellStyle(getGoldenBackgroundStyle(workbook));
										cell.setCellValue("Department");
										break;
									}
									case 6: {
										cell = row.createCell(k);
//										style.setFont(getHeadingFont(workbook));
										cell.setCellStyle(getGoldenBackgroundStyle(workbook));
										cell.setCellValue("Component name");
										break;
									}
									case 7: {
										cell = row.createCell(k);
//										style.setFont(getHeadingFont(workbook));
										cell.setCellStyle(getGoldenBackgroundStyle(workbook));
										cell.setCellValue("Approver");
										break;
									}
									case 8: {
										cell = row.createCell(k);
//										style.setFont(getHeadingFont(workbook));
										cell.setCellStyle(getGoldenBackgroundStyle(workbook));
										cell.setCellValue("Send by");
										break;
									}
									case 9: {
										cell = row.createCell(k);
//										style.setFont(getHeadingFont(workbook));
										cell.setCellStyle(getGoldenBackgroundStyle(workbook));
										cell.setCellValue("Status");
										break;
									}
									case 10: {
										cell = row.createCell(k);
//										style.setFont(getHeadingFont(workbook));
										cell.setCellStyle(getGoldenBackgroundStyle(workbook));
										cell.setCellValue("Description");
										break;
									}
									case 11: {
										cell = row.createCell(k);
//										style.setFont(getHeadingFont(workbook));
										cell.setCellStyle(getGoldenBackgroundStyle(workbook));
										cell.setCellValue("Development start date");
										break;
									}
									case 12: {
										cell = row.createCell(k);
//										style.setFont(getHeadingFont(workbook));
										cell.setCellStyle(getGoldenBackgroundStyle(workbook));
										cell.setCellValue("Development end date");
										break;
									}
									case 13: {
										cell = row.createCell(k);
//										style.setFont(getHeadingFont(workbook));
										cell.setCellStyle(getGoldenBackgroundStyle(workbook));
										cell.setCellValue("Test completion date");
										break;
									}
									case 14: {
										cell = row.createCell(k);
//										style.setFont(getHeadingFont(workbook));
										cell.setCellStyle(getGoldenBackgroundStyle(workbook));
										cell.setCellValue("Deployment date");
										break;
									}
									case 15: {
										cell = row.createCell(k);
//										style.setFont(getHeadingFont(workbook));
										cell.setCellStyle(getGoldenBackgroundStyle(workbook));
										cell.setCellValue("Impacted department");
										break;
									}

									}
								}
								rowNum = rowNum + 1;
								SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
								for (RecommendationResponseDto responseDto : completedRecommendationForThisDept) {
//											System.out.println(responseDto.getReferenceId());
									row = sheet1.createRow(rowNum);
									cell = row.createCell(0);
									cell.setCellValue(responseDto.getReferenceId());
									cell = row.createCell(1);
									String date = formatter
											.format(DateUtil.convertUTCTOIST(responseDto.getCreatedAt()));
									cell.setCellValue(date);
									cell = row.createCell(2);
									cell.setCellValue(responseDto.getRecommendationType().getName());
									cell = row.createCell(3);
									cell.setCellValue(responseDto.getPriority());
									cell = row.createCell(4);
									date = formatter.format(DateUtil.convertUTCTOIST(responseDto.getRecommendDate()));
									cell.setCellValue(date);
									cell = row.createCell(5);
									cell.setCellValue(responseDto.getDepartment().getName());
									cell = row.createCell(6);
									cell.setCellValue(responseDto.getComponent().getName());
									// set approver
									cell = row.createCell(7);
									if (responseDto.getApprover() != null) {
										cell.setCellValue(responseDto.getApprover().getUserName());
									} else {
										cell.setCellValue("--");
									}
									cell = row.createCell(8);
									cell.setCellValue(responseDto.getCreatedBy().getUserName());
									cell = row.createCell(9);
									cell.setCellValue(responseDto.getStatus().getStatusName());
									cell = row.createCell(10);
									cell.setCellValue(responseDto.getDescriptions());

									if (responseDto.getRecommendationDeploymentDetails() != null) {
										cell = row.createCell(11);
										cell.setCellValue(formatter.format(DateUtil.convertUTCTOIST(responseDto
												.getRecommendationDeploymentDetails().getDevelopmentStartDate())));
										cell = row.createCell(12);
										cell.setCellValue(formatter.format(DateUtil.convertUTCTOIST(responseDto
												.getRecommendationDeploymentDetails().getDevelopementEndDate())));
										cell = row.createCell(13);
										cell.setCellValue(formatter.format(DateUtil.convertUTCTOIST(responseDto
												.getRecommendationDeploymentDetails().getTestCompletionDate())));
										cell = row.createCell(14);
										cell.setCellValue(formatter.format(DateUtil.convertUTCTOIST(
												responseDto.getRecommendationDeploymentDetails().getDeploymentDate())));
										cell = row.createCell(15);
										cell.setCellValue(responseDto.getRecommendationDeploymentDetails()
												.getImpactedDepartment());
									} else {
										cell = row.createCell(11);
										cell.setCellValue("--");
										cell = row.createCell(12);
										cell.setCellValue("--");
										cell = row.createCell(13);
										cell.setCellValue("--");
										cell = row.createCell(14);
										cell.setCellValue("--");
										cell = row.createCell(15);
										cell.setCellValue("--");
									}
									rowNum = rowNum + 1;
								}
								rowNum = rowNum + 1;
							}

						}
					}

				}
				SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy");
				File tempFile = File.createTempFile("Mis_Data_" + formatter.format(new Date()), ".xlsx");

				FileOutputStream fos = new FileOutputStream(tempFile);
				workbook.write(fos);
				workbook.close();
				fos.close();
				response.setContentType("application/octet-stream");

				response.setHeader("Content-Disposition", "attachment; filename=\"filename.xlsx\"");

				byte[] fileContent = readFileToByteArray(tempFile);

				// Write the byte array to the response output stream
				OutputStream outputStream = response.getOutputStream();
				outputStream.write(fileContent);
				outputStream.flush();
			}

		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Font getHeadingFont(Workbook workbook) {
		Font font = workbook.createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 14);
		return font;
	}

	public Font getSubHeadingFont(Workbook workbook) {
		Font font = workbook.createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 11);
		return font;
	}

	public Font getTextFont(Workbook workbook) {
		Font font = workbook.createFont();
//		font.setBold(false);
		font.setFontHeightInPoints((short) 11);
		return font;
	}

	private byte[] readFileToByteArray(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		byte[] fileContent = new byte[(int) file.length()];
		fis.read(fileContent);
		fis.close();
		return fileContent;
	}

	public CellStyle getYellowBackgroundCellStyle(Workbook workbook) {
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		return cellStyle;
	}

	public CellStyle getRedBackgroundStyle(Workbook workbook) {
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		return cellStyle;
	}

	public CellStyle getBlueBackgroundStyle(Workbook workbook) {
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		return cellStyle;
	}

	public CellStyle getGreenBackgroundStyle(Workbook workbook) {
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		return cellStyle;
	}

	public CellStyle getLightGreenBackgroundStyle(Workbook workbook) {
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		return cellStyle;
	}

	public CellStyle getOrangeBackgroundStyle(Workbook workbook) {
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		return cellStyle;
	}

	public CellStyle getPinkBackgroundStyle(Workbook workbook) {
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFillForegroundColor(IndexedColors.PINK.getIndex());
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		return cellStyle;
	}

	public CellStyle getGreyBackgroundStyle(Workbook workbook) {
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		Font font = workbook.createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 14);
		cellStyle.setFont(font);
		return cellStyle;
	}

	public CellStyle getGoldenBackgroundStyle(Workbook workbook) {
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		Font font = workbook.createFont();
		font.setBold(true);
//		font.setFontHeightInPoints((short) 14);
		cellStyle.setFont(font);
		return cellStyle;
	}

}
