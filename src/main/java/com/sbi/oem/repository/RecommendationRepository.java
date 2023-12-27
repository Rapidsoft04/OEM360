package com.sbi.oem.repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sbi.oem.dto.SearchDto;
import com.sbi.oem.model.Recommendation;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

	Optional<Recommendation> findByReferenceId(String refId);

	@Query(value = "SELECT * FROM recommendation where department_id in (?1) order by updated_at desc", nativeQuery = true)
	List<Recommendation> findAllByDepartmentIdIn(List<Long> departmentIds);

	@Query(value = "SELECT * FROM recommendation where created_by=?1 order by updated_at desc", nativeQuery = true)
	List<Recommendation> findAllByUserId(Long id);

	List<Recommendation> findAll(Specification<Recommendation> specification);

    default List<Recommendation> findAllByUserIdFilter(Long id, SearchDto searchDto) {
        Specification<Recommendation> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (id != null) {            	
            	predicates.add(builder.equal(root.get("createdBy"), id));
            }

            if (searchDto.getRecommendationType() != null) {
                predicates.add(builder.equal(root.get("recommendationType"), searchDto.getRecommendationType()));
            }

            if (searchDto.getPriorityId() != null) {
                predicates.add(builder.equal(root.get("priorityId"), searchDto.getPriorityId()));
            }

            if (searchDto.getDepartmentId() != null) {
                predicates.add(builder.equal(root.get("department"), searchDto.getDepartmentId()));
            }

            if (searchDto.getStatusId() != null) {
                predicates.add(builder.equal(root.get("recommendationStatus").get("id"), searchDto.getStatusId()));
            }

            if (searchDto.getFromDate() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("recommendDate"), searchDto.getFromDate()));
            }

            if (searchDto.getToDate() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("recommendDate"), searchDto.getToDate()));
            }

            query.orderBy(builder.asc(root.get("updatedAt")));
            
            return builder.and(predicates.toArray(new Predicate[0]));
        };

        return findAll(specification);
    }
	
}
