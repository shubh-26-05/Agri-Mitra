package com.agm.agrimitra.repository;

import com.agm.agrimitra.entity.FertilizerUsage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FertilizerUsageRepository extends JpaRepository<FertilizerUsage, Long> {

    Page<FertilizerUsage> findByFieldIdOrderByApplicationDateDesc(Long fieldId, Pageable pageable);

    Page<FertilizerUsage> findByFieldId(Long fieldId, Pageable pageable);

    List<FertilizerUsage> findByFieldIdOrderByApplicationDateDesc(Long fieldId);
}
