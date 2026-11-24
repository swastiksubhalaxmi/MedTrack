package com.medistock.medistock_backend.repository;

import com.medistock.medistock_backend.entity.OrderStatus;
import com.medistock.medistock_backend.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);
    List<PurchaseOrder> findBySupplierId(Long supplierId);
    List<PurchaseOrder> findByStatus(OrderStatus status);
    List<PurchaseOrder> findByCreatedById(Long userId);

    @Query("SELECT DISTINCT po FROM PurchaseOrder po LEFT JOIN FETCH po.supplier LEFT JOIN FETCH po.createdBy LEFT JOIN FETCH po.items items LEFT JOIN FETCH items.supplierMedicine ORDER BY po.orderDate DESC")
    List<PurchaseOrder> findAllWithDetails();

    @Query("SELECT DISTINCT po FROM PurchaseOrder po LEFT JOIN FETCH po.supplier LEFT JOIN FETCH po.createdBy LEFT JOIN FETCH po.items items LEFT JOIN FETCH items.supplierMedicine WHERE po.supplier.id = :supplierId ORDER BY po.orderDate DESC")
    List<PurchaseOrder> findBySupplierIdWithDetails(@Param("supplierId") Long supplierId);

    @Query("SELECT DISTINCT po FROM PurchaseOrder po LEFT JOIN FETCH po.supplier LEFT JOIN FETCH po.createdBy ORDER BY po.orderDate DESC")
    List<PurchaseOrder> findTop5RecentOrders(org.springframework.data.domain.Pageable pageable);
}
