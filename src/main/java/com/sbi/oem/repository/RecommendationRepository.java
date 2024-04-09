package com.sbi.oem.repository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.Predicate;

import org.apache.http.impl.cookie.DateParseException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sbi.oem.dto.RecommendationAddRequestDto;
import com.sbi.oem.dto.SearchDto;
import com.sbi.oem.enums.StatusEnum;
import com.sbi.oem.model.Recommendation;
import com.sbi.oem.util.DateUtil;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

	@Query(value = "SELECT * FROM recommendation where ref_id=?1", nativeQuery = true)
	Optional<Recommendation> findByReferenceId(String refId);

	@Query(value = "SELECT * FROM recommendation where department_id in (?1) order by updated_at desc", nativeQuery = true)
	List<Recommendation> findAllByDepartmentIdIn(List<Long> departmentIds);

	@Query(value = "SELECT * FROM recommendation where created_by=?1 order by updated_at desc", nativeQuery = true)
	List<Recommendation> findAllByUserId(Long id);

	List<Recommendation> findAll(Specification<Recommendation> specification);

	Page<Recommendation> findAll(Specification<Recommendation> specification, Pageable pageable);

	default Page<Recommendation> findAllPendingRequestByPagination(SearchDto searchDto, Integer pageNumber,
			Integer pageSize) {
		try {

		} catch (Exception e) {
			// TODO: handle exception
		}
		Pageable pageable = PageRequest.of(pageNumber, pageSize);

		Specification<Recommendation> specification = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (searchDto.getRecommendationType() != null) {
				predicates
						.add(criteriaBuilder.equal(root.get("recommendationType"), searchDto.getRecommendationType()));
			}

			if (searchDto.getPriorityId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("priorityId"), searchDto.getPriorityId()));
			}

			if (searchDto.getReferenceId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("referenceId"), searchDto.getReferenceId()));
			}

			if (searchDto.getDepartmentId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("department"), searchDto.getDepartmentId()));
			}

			if (searchDto.getStatusId() != null) {
				predicates.add(
						criteriaBuilder.equal(root.get("recommendationStatus").get("id"), searchDto.getStatusId()));
			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() != null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date fromDate = null;
				try {
					fromDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getFromDate()));
				} catch (ParseException e) {

					e.printStackTrace();
				}
				Date toDate = null;
				try {
					toDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getToDate()));
				} catch (ParseException e) {

					e.printStackTrace();
				}
				predicates.add(criteriaBuilder.or(criteriaBuilder.between(root.get("updatedAt"), fromDate, toDate)));

			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() == null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date fromDate = null;
				try {
					fromDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getFromDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Date currentDate = DateUtil.convertISTtoUTC(new Date());
				predicates
						.add(criteriaBuilder.or(criteriaBuilder.between(root.get("updatedAt"), fromDate, currentDate)));
			}

			if (searchDto.getFromDate() == null && searchDto.getToDate() != null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date toDate = null;
				try {
					toDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getToDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("updatedAt"), toDate));
			}

			if (searchDto.getCreatedBy() != null) {
				predicates.add(criteriaBuilder.equal(root.get("createdBy").get("id"), searchDto.getCreatedBy()));
			}

			query.orderBy(criteriaBuilder.desc(root.get("updatedAt")));
			predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("isAppOwnerApproved"), false),
					criteriaBuilder.equal(root.get("isAppOwnerRejected"), false)));

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};

		Page<Recommendation> recommendationPage = findAll(specification, pageable);
		return recommendationPage;
	}

	default List<Recommendation> findAllPendingRecommendationsBySearchDto(SearchDto searchDto) {

		Specification<Recommendation> specification = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (searchDto.getRecommendationType() != null) {
				predicates
						.add(criteriaBuilder.equal(root.get("recommendationType"), searchDto.getRecommendationType()));
			}

			if (searchDto.getPriorityId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("priorityId"), searchDto.getPriorityId()));
			}

			if (searchDto.getReferenceId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("referenceId"), searchDto.getReferenceId()));
			}

			if (searchDto.getDepartmentId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("department"), searchDto.getDepartmentId()));
			}

			if (searchDto.getStatusId() != null) {
				predicates.add(
						criteriaBuilder.equal(root.get("recommendationStatus").get("id"), searchDto.getStatusId()));
			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() != null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date fromDate = null;
				try {
					fromDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getFromDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Date toDate = null;
				try {
					toDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getToDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				predicates.add(criteriaBuilder.or(criteriaBuilder.between(root.get("updatedAt"), fromDate, toDate)));

			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() == null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date fromDate = null;
				try {
					fromDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getFromDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Date currentDate = DateUtil.convertISTtoUTC(new Date());
				predicates
						.add(criteriaBuilder.or(criteriaBuilder.between(root.get("updatedAt"), fromDate, currentDate)));
			}

			if (searchDto.getFromDate() == null && searchDto.getToDate() != null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date toDate = null;
				try {
					toDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getToDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("updatedAt"), toDate));
			}

			if (searchDto.getCreatedBy() != null) {
				predicates.add(criteriaBuilder.equal(root.get("createdBy").get("id"), searchDto.getCreatedBy()));
			}
			if (searchDto.getSearchKey() != null) {
				predicates.add(criteriaBuilder.or(
						criteriaBuilder.like(root.get("descriptions"), "%" + searchDto.getSearchKey() + "%"),
						criteriaBuilder.like(root.get("referenceId"), "%" + searchDto.getSearchKey() + "%")));

			}
			query.orderBy(criteriaBuilder.desc(root.get("updatedAt")));

			predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("isAppOwnerApproved"), false),
					criteriaBuilder.equal(root.get("isAppOwnerRejected"), false)));

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
		return findAll(specification);
	}

	default Page<Recommendation> findAllApprovedRequestByPagination(SearchDto searchDto, Integer pageNumber,
			Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNumber, pageSize);

		Specification<Recommendation> specification = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (searchDto.getRecommendationType() != null) {
				predicates
						.add(criteriaBuilder.equal(root.get("recommendationType"), searchDto.getRecommendationType()));
			}

			if (searchDto.getPriorityId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("priorityId"), searchDto.getPriorityId()));
			}

			if (searchDto.getReferenceId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("referenceId"), searchDto.getReferenceId()));
			}

			if (searchDto.getDepartmentId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("department"), searchDto.getDepartmentId()));
			}

			if (searchDto.getStatusId() != null) {
				predicates.add(
						criteriaBuilder.equal(root.get("recommendationStatus").get("id"), searchDto.getStatusId()));
			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() != null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date fromDate = null;
				try {
					fromDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getFromDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Date toDate = null;
				try {
					toDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getToDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				predicates.add(criteriaBuilder.or(criteriaBuilder.between(root.get("updatedAt"), fromDate, toDate)));

			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() == null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date fromDate = null;
				try {
					fromDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getFromDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Date currentDate = DateUtil.convertISTtoUTC(new Date());
				predicates
						.add(criteriaBuilder.or(criteriaBuilder.between(root.get("updatedAt"), fromDate, currentDate)));
			}

			if (searchDto.getFromDate() == null && searchDto.getToDate() != null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date toDate = null;
				try {
					toDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getToDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("updatedAt"), toDate));
			}

			if (searchDto.getCreatedBy() != null) {
				predicates.add(criteriaBuilder.equal(root.get("createdBy").get("id"), searchDto.getCreatedBy()));
			}

			query.orderBy(criteriaBuilder.desc(root.get("updatedAt")));
			predicates.add(criteriaBuilder.equal(root.get("isAppOwnerApproved"), true));
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};

		Page<Recommendation> recommendationPage = findAll(specification, pageable);
		return recommendationPage;
	}

	default List<Recommendation> findAllApprovedRecommendationsBySearchDto(SearchDto searchDto) {
		Specification<Recommendation> specification = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (searchDto.getRecommendationType() != null) {
				predicates
						.add(criteriaBuilder.equal(root.get("recommendationType"), searchDto.getRecommendationType()));
			}

			if (searchDto.getPriorityId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("priorityId"), searchDto.getPriorityId()));
			}

			if (searchDto.getReferenceId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("referenceId"), searchDto.getReferenceId()));
			}

			if (searchDto.getDepartmentId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("department"), searchDto.getDepartmentId()));
			}

			if (searchDto.getStatusId() != null) {
				predicates.add(
						criteriaBuilder.equal(root.get("recommendationStatus").get("id"), searchDto.getStatusId()));
			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() != null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date fromDate = null;
				try {
					fromDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getFromDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Date toDate = null;
				try {
					toDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getToDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				predicates.add(criteriaBuilder.or(criteriaBuilder.between(root.get("updatedAt"), fromDate, toDate)));

			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() == null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date fromDate = null;
				try {
					fromDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getFromDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Date currentDate = DateUtil.convertISTtoUTC(new Date());
				predicates
						.add(criteriaBuilder.or(criteriaBuilder.between(root.get("updatedAt"), fromDate, currentDate)));
			}

			if (searchDto.getFromDate() == null && searchDto.getToDate() != null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date toDate = null;
				try {
					toDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getToDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("updatedAt"), toDate));
			}

			if (searchDto.getCreatedBy() != null) {
				predicates.add(criteriaBuilder.equal(root.get("createdBy").get("id"), searchDto.getCreatedBy()));
			}
			if (searchDto.getSearchKey() != null) {
				predicates.add(criteriaBuilder.or(
						criteriaBuilder.like(root.get("descriptions"), "%" + searchDto.getSearchKey() + "%"),
						criteriaBuilder.like(root.get("referenceId"), "%" + searchDto.getSearchKey() + "%")));

			}
			query.orderBy(criteriaBuilder.desc(root.get("updatedAt")));
			predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get("isAppOwnerApproved"), true),
					criteriaBuilder.equal(root.get("isAgmRejected"), true),
					criteriaBuilder.equal(root.get("isAppOwnerRejected"), true)));
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};

		return findAll(specification);
	}

	default Page<Recommendation> findAllRecommendationsOemAndAgmPagination(Long id, SearchDto searchDto,
			long pageNumber, long pageSize) {
		Specification<Recommendation> specification = (root, query, criteriaBuilder) -> {

			List<Predicate> predicates = new ArrayList<>();

			if (id != null) {
				predicates.add(criteriaBuilder.equal(root.get("createdBy"), id));
			}

			if (searchDto.getRecommendationType() != null) {
				predicates
						.add(criteriaBuilder.equal(root.get("recommendationType"), searchDto.getRecommendationType()));
			}

			if (searchDto.getPriorityId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("priorityId"), searchDto.getPriorityId()));
			}

			if (searchDto.getDepartmentId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("department"), searchDto.getDepartmentId()));
			}

			if (searchDto.getStatusId() != null) {
				predicates.add(
						criteriaBuilder.equal(root.get("recommendationStatus").get("id"), searchDto.getStatusId()));
			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() != null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date fromDate = null;
				try {
					fromDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getFromDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Date toDate = null;
				try {
					toDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getToDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				predicates.add(criteriaBuilder.or(criteriaBuilder.between(root.get("updatedAt"), fromDate, toDate)));

			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() == null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date fromDate = null;
				try {
					fromDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getFromDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Date currentDate = DateUtil.convertISTtoUTC(new Date());
				predicates
						.add(criteriaBuilder.or(criteriaBuilder.between(root.get("updatedAt"), fromDate, currentDate)));
			}

			if (searchDto.getFromDate() == null && searchDto.getToDate() != null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date toDate = null;
				try {
					toDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getToDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("updatedAt"), toDate));
			}

			query.orderBy(criteriaBuilder.desc(root.get("updatedAt")));
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};

		Pageable pageable = PageRequest.of((int) pageNumber, (int) pageSize);
		Page<Recommendation> recommendationPage = findAll(specification, pageable);
		long totalElements = recommendationPage.getTotalElements();
		return new PageImpl<>(recommendationPage.getContent(), pageable, totalElements);

	}

	default Page<Recommendation> findAllPendingRecommendationsForAgmBySearchDtoPagination(SearchDto searchDto,
			long pageNumber, long pageSize) {

		Specification<Recommendation> specification = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (searchDto.getRecommendationType() != null) {
				predicates
						.add(criteriaBuilder.equal(root.get("recommendationType"), searchDto.getRecommendationType()));
			}

			if (searchDto.getPriorityId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("priorityId"), searchDto.getPriorityId()));
			}

			if (searchDto.getReferenceId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("referenceId"), searchDto.getReferenceId()));
			}

			if (searchDto.getDepartmentId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("department"), searchDto.getDepartmentId()));
			}

			if (searchDto.getStatusId() != null) {
				predicates.add(
						criteriaBuilder.equal(root.get("recommendationStatus").get("id"), searchDto.getStatusId()));
			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() != null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date fromDate = null;
				try {
					fromDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getFromDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Date toDate = null;
				try {
					toDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getToDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				predicates.add(criteriaBuilder.or(criteriaBuilder.between(root.get("updatedAt"), fromDate, toDate)));

			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() == null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date fromDate = null;
				try {
					fromDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getFromDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Date currentDate = DateUtil.convertISTtoUTC(new Date());
				predicates
						.add(criteriaBuilder.or(criteriaBuilder.between(root.get("updatedAt"), fromDate, currentDate)));
			}

			if (searchDto.getFromDate() == null && searchDto.getToDate() != null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date toDate = null;
				try {
					toDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getToDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("updatedAt"), toDate));
			}

			if (searchDto.getCreatedBy() != null) {
				predicates.add(criteriaBuilder.equal(root.get("createdBy").get("id"), searchDto.getCreatedBy()));
			}

			query.orderBy(criteriaBuilder.desc(root.get("updatedAt")));

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};

		Pageable pageable = PageRequest.of((int) pageNumber, (int) pageSize);
		Page<Recommendation> recommendationPage = findAll(specification, pageable);
		long totalElements = recommendationPage.getTotalElements();
		return new PageImpl<>(recommendationPage.getContent(), pageable, totalElements);

	}

	default Page<Recommendation> findAllRecommendationsForGmBySearchDtoPagination(SearchDto searchDto, long pageNumber,
			long pageSize) {

		Specification<Recommendation> specification = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (searchDto.getRecommendationType() != null) {
				predicates
						.add(criteriaBuilder.equal(root.get("recommendationType"), searchDto.getRecommendationType()));
			}

			if (searchDto.getPriorityId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("priorityId"), searchDto.getPriorityId()));
			}

			if (searchDto.getReferenceId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("referenceId"), searchDto.getReferenceId()));
			}

			if (searchDto.getDepartmentId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("department"), searchDto.getDepartmentId()));
			}

			if (searchDto.getStatusId() != null) {
				predicates.add(
						criteriaBuilder.equal(root.get("recommendationStatus").get("id"), searchDto.getStatusId()));
			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() != null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date fromDate = null;
				try {
					fromDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getFromDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Date toDate = null;
				try {
					toDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getToDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				predicates.add(criteriaBuilder.or(criteriaBuilder.between(root.get("updatedAt"), fromDate, toDate)));

			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() == null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date fromDate = null;
				try {
					fromDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getFromDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Date currentDate = DateUtil.convertISTtoUTC(new Date());
				predicates
						.add(criteriaBuilder.or(criteriaBuilder.between(root.get("updatedAt"), fromDate, currentDate)));
			}

			if (searchDto.getFromDate() == null && searchDto.getToDate() != null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date toDate = null;
				try {
					toDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getToDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("updatedAt"), toDate));
			}

			if (searchDto.getCreatedBy() != null) {
				predicates.add(criteriaBuilder.equal(root.get("createdBy").get("id"), searchDto.getCreatedBy()));
			}

			query.orderBy(criteriaBuilder.desc(root.get("updatedAt")));

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
		Pageable pageable = PageRequest.of((int) pageNumber, (int) pageSize);
		Page<Recommendation> recommendationPage = findAll(specification, pageable);
		long totalElements = recommendationPage.getTotalElements();
		return new PageImpl<>(recommendationPage.getContent(), pageable, totalElements);
	}

	default List<Recommendation> findAllRecommendationsOemAndAgmBySearchDto(Long id, SearchDto searchDto) {

		Specification<Recommendation> specification = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (id != null) {
				predicates.add(criteriaBuilder.equal(root.get("createdBy"), id));
			}

			if (searchDto.getRecommendationType() != null) {
				predicates
						.add(criteriaBuilder.equal(root.get("recommendationType"), searchDto.getRecommendationType()));
			}

			if (searchDto.getPriorityId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("priorityId"), searchDto.getPriorityId()));
			}

			if (searchDto.getDepartmentId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("department"), searchDto.getDepartmentId()));
			}

			if (searchDto.getStatusId() != null) {
				predicates.add(
						criteriaBuilder.equal(root.get("recommendationStatus").get("id"), searchDto.getStatusId()));
			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() != null) {
				try {
					Date fromDate = new SimpleDateFormat("yyyy-MM-dd").parse(searchDto.getFromDate());
					Date toDate = new SimpleDateFormat("yyyy-MM-dd").parse(searchDto.getToDate());

					predicates.add(criteriaBuilder.between(root.get("recommendDate"), fromDate, toDate));
				} catch (ParseException e) {
					e.printStackTrace();
					// Handle or log the parsing exception
				}
			} else if (searchDto.getFromDate() != null) {
				try {
					Date fromDate = new SimpleDateFormat("yyyy-MM-dd").parse(searchDto.getFromDate());
					predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("recommendDate"), fromDate));
				} catch (ParseException e) {
					e.printStackTrace();
					// Handle or log the parsing exception
				}
			} else if (searchDto.getToDate() != null) {
				try {
					Date toDate = new SimpleDateFormat("yyyy-MM-dd").parse(searchDto.getToDate());
					predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("recommendDate"), searchDto.getToDate()));
				} catch (ParseException e) {
					e.printStackTrace();
					// Handle or log the parsing exception
				}
			}

			if (searchDto.getSearchKey() != null) {
				predicates.add(criteriaBuilder.or(
						criteriaBuilder.like(root.get("descriptions"), "%" + searchDto.getSearchKey() + "%"),
						criteriaBuilder.like(root.get("referenceId"), "%" + searchDto.getSearchKey() + "%")));
			}

			query.orderBy(criteriaBuilder.desc(root.get("updatedAt")));
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};

		return findAll(specification);
	}

	default List<Recommendation> findAllRecommendationsForGmBySearchDto(SearchDto searchDto) {

		Specification<Recommendation> specification = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (searchDto.getRecommendationType() != null) {
				predicates
						.add(criteriaBuilder.equal(root.get("recommendationType"), searchDto.getRecommendationType()));
			}

			if (searchDto.getPriorityId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("priorityId"), searchDto.getPriorityId()));
			}

			if (searchDto.getReferenceId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("referenceId"), searchDto.getReferenceId()));
			}

			if (searchDto.getDepartmentId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("department"), searchDto.getDepartmentId()));
			}

			if (searchDto.getStatusId() != null) {
				predicates.add(
						criteriaBuilder.equal(root.get("recommendationStatus").get("id"), searchDto.getStatusId()));
			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() != null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date fromDate = null;
				try {
					fromDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getFromDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Date toDate = null;
				try {
					toDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getToDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				predicates.add(criteriaBuilder.or(criteriaBuilder.between(root.get("createdAt"), fromDate, toDate)));

			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() == null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date fromDate = null;
				try {
					fromDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getFromDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Date currentDate = DateUtil.convertISTtoUTC(new Date());
				predicates
						.add(criteriaBuilder.or(criteriaBuilder.between(root.get("createdAt"), fromDate, currentDate)));
			}

			if (searchDto.getFromDate() == null && searchDto.getToDate() != null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date toDate = null;
				try {
					toDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getToDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), toDate));
			}

			if (searchDto.getCreatedBy() != null) {
				predicates.add(criteriaBuilder.equal(root.get("createdBy").get("id"), searchDto.getCreatedBy()));
			}
			if (searchDto.getSearchKey() != null) {
				predicates.add(criteriaBuilder.or(
						criteriaBuilder.like(root.get("descriptions"), "%" + searchDto.getSearchKey() + "%"),
						criteriaBuilder.like(root.get("referenceId"), "%" + searchDto.getSearchKey() + "%")));

			}
			query.orderBy(criteriaBuilder.desc(root.get("updatedAt")));

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
		return findAll(specification);
	}

	@Query(value = "SELECT * FROM recommendation where department_id=?1 and created_at between ?2 and ?3", nativeQuery = true)
	List<Recommendation> findByAgmIdAndUpdatedAtBetween(Long id, String fromDate, String toDate);

	@Query(value = "SELECT * FROM recommendation where created_at between ?1 and ?2", nativeQuery = true)
	List<Recommendation> getAllDataForGMAndUpdatedAtBetween(String fromDate, String toDate);

	@Query(value = "SELECT * FROM recommendation where department_id=?1 and created_at<?2", nativeQuery = true)
	List<Recommendation> findAllByDepartmentIdAndCreatedAtBetweenToday(Long id, String toDate);

	@Query(value = "SELECT * FROM recommendation where created_at<?1", nativeQuery = true)
	List<Recommendation> getAllDataForGMAndCreatedAtBetweenToday(String toDate);

	default List<Recommendation> findAllPendingRecommendationsForAgmBySearchDto(SearchDto searchDto) {

		Specification<Recommendation> specification = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (searchDto.getRecommendationType() != null) {
				predicates
						.add(criteriaBuilder.equal(root.get("recommendationType"), searchDto.getRecommendationType()));
			}

			if (searchDto.getPriorityId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("priorityId"), searchDto.getPriorityId()));
			}

			if (searchDto.getReferenceId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("referenceId"), searchDto.getReferenceId()));
			}
			if (searchDto.getDepartmentIds() != null) {
				predicates.add(root.get("department").in(searchDto.getDepartmentIds()));
			}
			if (searchDto.getDepartmentId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("department"), searchDto.getDepartmentId()));
			}

			if (searchDto.getStatusId() != null) {
				predicates.add(
						criteriaBuilder.equal(root.get("recommendationStatus").get("id"), searchDto.getStatusId()));
			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() != null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date fromDate = null;
				try {
					fromDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getFromDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Date toDate = null;
				try {
					toDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getToDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				predicates.add(criteriaBuilder.or(criteriaBuilder.between(root.get("createdAt"), fromDate, toDate)));

			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() == null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date fromDate = null;
				try {
					fromDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getFromDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Date currentDate = DateUtil.convertISTtoUTC(new Date());
				predicates
						.add(criteriaBuilder.or(criteriaBuilder.between(root.get("createdAt"), fromDate, currentDate)));
			}

			if (searchDto.getFromDate() == null && searchDto.getToDate() != null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date toDate = null;
				try {
					toDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getToDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), toDate));
			}

			if (searchDto.getCreatedBy() != null) {
				predicates.add(criteriaBuilder.equal(root.get("createdBy").get("id"), searchDto.getCreatedBy()));
			}
			if (searchDto.getSearchKey() != null) {
				predicates.add(criteriaBuilder.or(
						criteriaBuilder.like(root.get("descriptions"), "%" + searchDto.getSearchKey() + "%"),
						criteriaBuilder.like(root.get("referenceId"), "%" + searchDto.getSearchKey() + "%")));

			}
			query.orderBy(criteriaBuilder.desc(root.get("updatedAt")));

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
		return findAll(specification);
	}

	default List<Recommendation> findAllApprovedRecommendationsOfAgmBySearchDto(SearchDto searchDto) {

		Specification<Recommendation> specification = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (searchDto.getRecommendationType() != null) {
				predicates
						.add(criteriaBuilder.equal(root.get("recommendationType"), searchDto.getRecommendationType()));
			}

			if (searchDto.getPriorityId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("priorityId"), searchDto.getPriorityId()));
			}

			if (searchDto.getReferenceId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("referenceId"), searchDto.getReferenceId()));
			}

			if (searchDto.getDepartmentId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("department"), searchDto.getDepartmentId()));
			}

			if (searchDto.getStatusId() != null) {
				predicates.add(
						criteriaBuilder.equal(root.get("recommendationStatus").get("id"), searchDto.getStatusId()));
			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() != null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date fromDate = null;
				try {
					fromDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getFromDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Date toDate = null;
				try {
					toDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getToDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				predicates.add(criteriaBuilder.or(criteriaBuilder.between(root.get("createdAt"), fromDate, toDate)));

			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() == null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date fromDate = null;
				try {
					fromDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getFromDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Date currentDate = DateUtil.convertISTtoUTC(new Date());
				predicates
						.add(criteriaBuilder.or(criteriaBuilder.between(root.get("createdAt"), fromDate, currentDate)));
			}

			if (searchDto.getFromDate() == null && searchDto.getToDate() != null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date toDate = null;
				try {
					toDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getToDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), toDate));
			}

			if (searchDto.getCreatedBy() != null) {
				predicates.add(criteriaBuilder.equal(root.get("createdBy").get("id"), searchDto.getCreatedBy()));
			}
			if (searchDto.getSearchKey() != null) {
				predicates.add(criteriaBuilder.or(
						criteriaBuilder.like(root.get("descriptions"), "%" + searchDto.getSearchKey() + "%"),
						criteriaBuilder.like(root.get("referenceId"), "%" + searchDto.getSearchKey() + "%")));

			}
			query.orderBy(criteriaBuilder.desc(root.get("updatedAt")));

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
		return findAll(specification);
	}

	default List<Recommendation> findAllApprovedRecommendationsOfDgmBySearchDto(SearchDto searchDto) {
		Specification<Recommendation> specification = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (searchDto.getRecommendationType() != null) {
				predicates
						.add(criteriaBuilder.equal(root.get("recommendationType"), searchDto.getRecommendationType()));
			}
			if (searchDto.getDepartmentIds() != null) {
				predicates.add(root.get("department").in(searchDto.getDepartmentIds()));
			}
			if (searchDto.getPriorityId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("priorityId"), searchDto.getPriorityId()));
			}

			if (searchDto.getReferenceId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("referenceId"), searchDto.getReferenceId()));
			}

			if (searchDto.getDepartmentId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("department"), searchDto.getDepartmentId()));
			}

			if (searchDto.getStatusId() != null && searchDto.getStatusId() <= StatusEnum.Released.getId()) {

				predicates.add(
						criteriaBuilder.equal(root.get("recommendationStatus").get("id"), searchDto.getStatusId()));

			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() != null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date fromDate = null;
				try {
					fromDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getFromDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Date toDate = null;
				try {
					toDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getToDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				predicates.add(criteriaBuilder.or(criteriaBuilder.between(root.get("updatedAt"), fromDate, toDate)));

			}

			if (searchDto.getFromDate() != null && searchDto.getToDate() == null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date fromDate = null;
				try {
					fromDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getFromDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Date currentDate = DateUtil.convertISTtoUTC(new Date());
				predicates
						.add(criteriaBuilder.or(criteriaBuilder.between(root.get("updatedAt"), fromDate, currentDate)));
			}
			if (searchDto.getSearchKey() != null) {
				predicates.add(criteriaBuilder.or(
						criteriaBuilder.like(root.get("descriptions"), "%" + searchDto.getSearchKey() + "%"),
						criteriaBuilder.like(root.get("referenceId"), "%" + searchDto.getSearchKey() + "%")));

			}
			if (searchDto.getFromDate() == null && searchDto.getToDate() != null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date toDate = null;
				try {
					toDate = DateUtil.convertISTtoUTC(formatter.parse(searchDto.getToDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("updatedAt"), toDate));
			}

			if (searchDto.getCreatedBy() != null) {
				predicates.add(criteriaBuilder.equal(root.get("createdBy").get("id"), searchDto.getCreatedBy()));
			}

			predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get("isAgmApproved"), true),
					criteriaBuilder.equal(root.get("isAgmRejected"), true),
					criteriaBuilder.equal(root.get("isAppOwnerApproved"), true),
					criteriaBuilder.equal(root.get("isAppOwnerApproved"), false)));
			query.orderBy(criteriaBuilder.desc(root.get("updatedAt")));
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};

		return findAll(specification);
	}

	default List<Recommendation> findAllRecommendationsByData(RecommendationAddRequestDto requestDto) {
		Specification<Recommendation> specification = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (requestDto.getDescription() != null) {
				predicates.add(criteriaBuilder.equal(root.get("descriptions"), requestDto.getDescription()));
			}

			if (requestDto.getTypeId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("recommendationType"), requestDto.getTypeId()));
			}

			if (requestDto.getPriorityId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("priorityId"), requestDto.getPriorityId()));
			}

			if (requestDto.getRecommendDate() != null) {
				predicates.add(criteriaBuilder.equal(root.get("recommendDate"), requestDto.getRecommendDate()));
			}

			if (requestDto.getComponentId() != null) {
				predicates.add(criteriaBuilder.equal(root.get("component"), requestDto.getComponentId()));
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};

		return findAll(specification);
	}

}
