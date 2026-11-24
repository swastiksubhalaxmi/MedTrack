package com.medistock.medistock_backend.repository;

import com.medistock.medistock_backend.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByMedicineId(Long medicineId);

    @Query("SELECT i FROM Inventory i WHERE i.quantity > 0 AND i.quantity <= i.reorderLevel")
    List<Inventory> findLowStockItems();

    @Query("SELECT i FROM Inventory i JOIN FETCH i.medicine")
    List<Inventory> findAllWithMedicine();

    @Query("SELECT i FROM Inventory i JOIN FETCH i.medicine WHERE i.quantity > 0 AND i.quantity <= i.reorderLevel")
    List<Inventory> findLowStockItemsWithMedicine();

    @Query("SELECT COALESCE(SUM(m.price * i.quantity), 0) FROM Inventory i JOIN i.medicine m")
    BigDecimal calculateTotalInventoryValue();
}
