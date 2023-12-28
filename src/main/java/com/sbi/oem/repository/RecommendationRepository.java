package com.sbi.oem.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sbi.oem.dto.SearchDto;
import com.sbi.oem.model.Recommendation;
import com.sbi.oem.util.DateUtil;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

	Optional<Recommendation> findByReferenceId(String refId);

	@Query(value = "SELECT * FROM recommendation where department_id in (?1) order by updated_at desc", nativeQuery = true)
	List<Recommendation> findAllByDepartmentIdIn(List<Long> departmentIds);

	@Query(value = "SELECT * FROM recommendation where created_by=?1 order by updated_at desc", nativeQuery = true)
	List<Recommendation> findAllByUserId(Long id);

	List<Recommendation> findAll(Specification<Recommendation> specification);

	default List<Recommendation> findAllPendingRecommendationsBySearchDto(SearchDto searchDto) {
		Specification<Recommendation> specification = (root, query, criteriacriteriacriteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (searchDto.getRecommendationType() != null) {
				predicates
						.add(criteriacriteriacriteriaBuilder.equal(root.get("recommendationType"), searchDto.getRecommendationType()));
			}

			if (searchDto.getPriorityId() != null) {
				predicates.add(criteriacriteriacriteriaBuilder.equal(root.get("priorityId"), searchDto.getPriorityId()));
			}

			if (searchDto.getReferenceId() != null) {
				predicates.add(criteriacriteriacriteriaBuilder.equal(root.get("referenceId"), searchDto.getReferenceId()));
			}

			if (searchDto.getDepartmentId() != null) {
				predicates.add(criteriacriteriacriteriaBuilder.equal(root.get("department"), searchDto.getDepartmentId()));
			}

			if (searchDto.getStatusId() != null) {
				predicates.add(
						criteriacriteriacriteriaBuilder.equal(root.get("recommendationStatus").get("id"), searchDto.getStatusId()));
			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() != null) {
				Date fromDate = DateUtil.convertISTtoUTC(searchDto.getFromDate());
				Date toDate = DateUtil.convertISTtoUTC(searchDto.getToDate());
				predicates.add(criteriacriteriacriteriaBuilder.or(criteriacriteriacriteriaBuilder.between(root.get("updatedAt"), fromDate, toDate)));

			}
			
			if(searchDto.getFromDate() != null && searchDto.getToDate() == null) {
				Date fromDate = DateUtil.convertISTtoUTC(searchDto.getFromDate());
				Date currentDate = DateUtil.convertISTtoUTC(new Date());
				predicates.add(criteriacriteriacriteriaBuilder.or(criteriacriteriacriteriaBuilder.between(root.get("updatedAt"), fromDate, currentDate)));
			}
			
			if(searchDto.getFromDate() == null && searchDto.getToDate() != null) {
				Date toDate = DateUtil.convertISTtoUTC(searchDto.getToDate());
				predicates.add(criteriacriteriacriteriaBuilder.lessThanOrEqualTo(root.get("updatedAt"), toDate));
			}
			

			if (searchDto.getCreatedBy() != null) {
				predicates.add(criteriacriteriacriteriaBuilder.equal(root.get("createdBy").get("id"), searchDto.getCreatedBy()));
			}

			query.orderBy(criteriacriteriacriteriaBuilder.desc(root.get("updatedAt")));
			predicates.add(criteriacriteriacriteriaBuilder.or(criteriacriteriacriteriaBuilder.isNull(root.get("isAppOwnerApproved")),
					criteriacriteriacriteriaBuilder.equal(root.get("isAppOwnerApproved"), false)));

			return criteriacriteriacriteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};

		return findAll(specification);
	}

	default List<Recommendation> findAllApprovedRecommendationsBySearchDto(SearchDto searchDto) {
		Specification<Recommendation> specification = (root, query, criteriacriteriacriteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (searchDto.getRecommendationType() != null) {
				predicates
						.add(criteriacriteriacriteriaBuilder.equal(root.get("recommendationType"), searchDto.getRecommendationType()));
			}

			if (searchDto.getPriorityId() != null) {
				predicates.add(criteriacriteriacriteriaBuilder.equal(root.get("priorityId"), searchDto.getPriorityId()));
			}

			if (searchDto.getReferenceId() != null) {
				predicates.add(criteriacriteriacriteriaBuilder.equal(root.get("referenceId"), searchDto.getReferenceId()));
			}

			if (searchDto.getDepartmentId() != null) {
				predicates.add(criteriacriteriacriteriaBuilder.equal(root.get("department"), searchDto.getDepartmentId()));
			}

			if (searchDto.getStatusId() != null) {
				predicates.add(
						criteriacriteriacriteriaBuilder.equal(root.get("recommendationStatus").get("id"), searchDto.getStatusId()));
			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() != null) {
				Date fromDate = DateUtil.convertISTtoUTC(searchDto.getFromDate());
				Date toDate = DateUtil.convertISTtoUTC(searchDto.getToDate());
				predicates.add(criteriacriteriacriteriaBuilder.or(criteriacriteriacriteriaBuilder.between(root.get("updatedAt"), fromDate, toDate)));

			}
			
			if(searchDto.getFromDate() != null && searchDto.getToDate() == null) {
				Date fromDate = DateUtil.convertISTtoUTC(searchDto.getFromDate());
				Date currentDate = DateUtil.convertISTtoUTC(new Date());
				predicates.add(criteriacriteriacriteriaBuilder.or(criteriacriteriacriteriaBuilder.between(root.get("updatedAt"), fromDate, currentDate)));
			}
			
			if(searchDto.getFromDate() == null && searchDto.getToDate() != null) {
				Date toDate = DateUtil.convertISTtoUTC(searchDto.getToDate());
				predicates.add(criteriacriteriacriteriaBuilder.lessThanOrEqualTo(root.get("updatedAt"), toDate));
			}

			if (searchDto.getCreatedBy() != null) {
				predicates.add(criteriacriteriacriteriaBuilder.equal(root.get("createdBy").get("id"), searchDto.getCreatedBy()));
			}

			query.orderBy(criteriacriteriacriteriaBuilder.desc(root.get("updatedAt")));
			predicates.add(criteriacriteriacriteriaBuilder.equal(root.get("isAppOwnerApproved"), true));

			return criteriacriteriacriteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};

		return findAll(specification);
	}

	default List<Recommendation> findAllByUserIdFilter(Long id, SearchDto searchDto) {
		Specification<Recommendation> specification = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (id != null) {
				predicates.add(criteriaBuilder.equal(root.get("createdBy"), id));
			}

			if (searchDto.getRecommendationType() != null) {
				predicates.add(criteriaBuilder.equal(root.get("recommendationType"), searchDto.getRecommendationType()));
			}

			if (searchDto.getPriorityId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("priorityId"), searchDto.getPriorityId()));
			}

			if (searchDto.getDepartmentId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("department"), searchDto.getDepartmentId()));
			}

			if (searchDto.getStatusId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("recommendationStatus").get("id"), searchDto.getStatusId()));
			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() != null) {
				Date fromDate = DateUtil.convertISTtoUTC(searchDto.getFromDate());
				Date toDate = DateUtil.convertISTtoUTC(searchDto.getToDate());
				predicates.add(criteriaBuilder.or(criteriaBuilder.between(root.get("updatedAt"), fromDate, toDate)));

			}
			
			if(searchDto.getFromDate() != null && searchDto.getToDate() == null) {
				Date fromDate = DateUtil.convertISTtoUTC(searchDto.getFromDate());
				Date currentDate = DateUtil.convertISTtoUTC(new Date());
				predicates.add(criteriaBuilder.or(criteriaBuilder.between(root.get("updatedAt"), fromDate, currentDate)));
			}
			
			if(searchDto.getFromDate() == null && searchDto.getToDate() != null) {
				Date toDate = DateUtil.convertISTtoUTC(searchDto.getToDate());
				predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("updatedAt"), toDate));
			}

			query.orderBy(criteriaBuilder.desc(root.get("updatedAt")));

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};

		return findAll(specification);
	}

}
