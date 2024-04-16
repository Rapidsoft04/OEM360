package com.sbi.oem.service;

import com.sbi.oem.dto.Response;
import com.sbi.oem.dto.SearchDto;

public interface MisReportService {

	Response<?> exportMisReportData(SearchDto searchDto);

}
