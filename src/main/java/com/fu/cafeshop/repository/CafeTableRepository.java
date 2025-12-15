package com.fu.cafeshop.repository;

import com.fu.cafeshop.entity.CafeTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CafeTableRepository extends JpaRepository<CafeTable, Long> {

    List<CafeTable> findByIsActiveTrueOrderByTableNumberAsc();

    List<CafeTable> findAllByOrderByTableNumberAsc();

    Optional<CafeTable> findByTableNumber(Integer tableNumber);

    boolean existsByTableNumber(Integer tableNumber);
}


