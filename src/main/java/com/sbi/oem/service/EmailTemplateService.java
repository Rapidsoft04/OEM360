package com.sbi.oem.service;

import org.springframework.stereotype.Service;

import com.sbi.oem.dto.Response;
import com.sbi.oem.model.Recommendation;

@Service
public interface EmailTemplateService {
	
	
	  Response<?> sendMail(Recommendation recommendation);

}
