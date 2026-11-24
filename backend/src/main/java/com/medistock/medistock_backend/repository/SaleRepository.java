package com.medistock.medistock_backend.repository;

import com.medistock.medistock_backend.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findAllByOrderBySaleDateDesc();

    @Query("SELECT DISTINCT s FROM Sale s LEFT JOIN FETCH s.items i LEFT JOIN FETCH i.medicine LEFT JOIN FETCH s.user ORDER BY s.saleDate DESC")
    List<Sale> findAllWithDetails();

    @Query("SELECT DISTINCT s FROM Sale s LEFT JOIN FETCH s.items i LEFT JOIN FETCH i.medicine LEFT JOIN FETCH s.user ORDER BY s.saleDate DESC")
    List<Sale> findTopSalesWithDetails(org.springframework.data.domain.Pageable pageable);
}
