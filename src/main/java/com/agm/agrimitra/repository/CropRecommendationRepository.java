package com.agm.agrimitra.repository;

import com.agm.agrimitra.entity.CropRecommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CropRecommendationRepository extends JpaRepository<CropRecommendation, Long> {

    List<CropRecommendation> findByFieldIdOrderByRecommendationDateDesc(Long fieldId);

    Page<CropRecommendation> findByFieldId(Long fieldId, Pageable pageable);
}
