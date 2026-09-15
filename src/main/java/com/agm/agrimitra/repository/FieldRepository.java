package com.agm.agrimitra.repository;

import com.agm.agrimitra.entity.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldRepository extends JpaRepository<Field, Long> {

    List<Field> findByFarmerId(Long farmerId);
}
