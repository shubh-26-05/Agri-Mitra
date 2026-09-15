package com.agm.agrimitra.repository;

import com.agm.agrimitra.entity.SoilData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoilDataRepository extends JpaRepository<SoilData, Long> {

    List<SoilData> findByFieldIdOrderByTestedDateDesc(Long fieldId);

    Page<SoilData> findByFieldId(Long fieldId, Pageable pageable);
}
