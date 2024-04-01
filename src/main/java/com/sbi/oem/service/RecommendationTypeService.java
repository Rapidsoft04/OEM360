package com.sbi.oem.service;

import com.sbi.oem.dto.Response;
import com.sbi.oem.model.RecommendationType;

public interface RecommendationTypeService {

	Response<?> save(RecommendationType recommendationType);

	Response<?> getAllByCompanyId();

	Response<?> update(RecommendationType recommendationType);
}
