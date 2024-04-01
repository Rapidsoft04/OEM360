package com.sbi.oem.service;

import com.sbi.oem.dto.Response;

public interface FunctionalityService {
	
	Response<?> getAll();
	
	Response<?> getAllComponentsList();
}
