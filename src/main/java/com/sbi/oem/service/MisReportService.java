package com.sbi.oem.service;

import javax.servlet.http.HttpServletResponse;

import com.sbi.oem.dto.Response;
import com.sbi.oem.dto.SearchDto;

public interface MisReportService {

	Response<?> exportMisReportData(SearchDto searchDto);

	void exportMisReportDataV2(SearchDto searchDto,HttpServletResponse response);


}
