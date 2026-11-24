package com.medistock.medistock_backend.service.impl;

import com.medistock.medistock_backend.dto.*;
import com.medistock.medistock_backend.repository.*;
import com.medistock.medistock_backend.service.DashboardService;
import com.medistock.medistock_backend.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final MedicineRepository medicineRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InventoryRepository inventoryRepository;
    private final SaleRepository saleRepository;
    private final InventoryService inventoryService;

    @Value("${inventory.near-expiry-days:30}")
    private int nearExpiryDays;

    @Override
    @Transactional(readOnly = true)
    @Cacheable("dashboardSummary")
    public DashboardSummaryDto getDashboardSummary() {
        long totalUsers = userRepository.countNormalUsers();
        long totalMedicines = medicineRepository.count();
        long totalSuppliers = supplierRepository.count();
        long totalOrders = purchaseOrderRepository.count();

        LocalDate today = LocalDate.now();
        LocalDate nearExpiryDate = today.plusDays(nearExpiryDays);

        // Fetch all 5 stock counts in 1 consolidated database round-trip
        long availableCount = 0;
        long lowStockMedicinesCount = 0;
        long outOfStockCount = 0;
        long nearExpiryCount = 0;
        long expiredCount = 0;

        List<Object[]> counts = medicineRepository.getStockSummaryCounts(today, nearExpiryDate);
        if (counts != null && !counts.isEmpty() && counts.get(0) != null) {
            Object[] row = counts.get(0);
            availableCount = ((Number) row[0]).longValue();
            lowStockMedicinesCount = ((Number) row[1]).longValue();
            outOfStockCount = ((Number) row[2]).longValue();
            nearExpiryCount = ((Number) row[3]).longValue();
            expiredCount = ((Number) row[4]).longValue();
        }

        List<InventoryResponse> lowStockItems = inventoryService.getLowStockInventory();

        // Calculate total inventory monetary value via single SQL aggregate query
        BigDecimal totalValue = inventoryRepository.calculateTotalInventoryValue();
        if (totalValue == null) {
            totalValue = BigDecimal.ZERO;
        }

        // Fetch only top 5 recent orders with supplier joined in 1 query
        List<PurchaseOrderResponse> recentOrders = purchaseOrderRepository.findTop5RecentOrders(PageRequest.of(0, 5)).stream()
                .map(order -> PurchaseOrderResponse.builder()
                        .id(order.getId())
                        .orderNumber(order.getOrderNumber())
                        .supplier(order.getSupplier() != null ? SupplierDto.builder()
                                .id(order.getSupplier().getId())
                                .name(order.getSupplier().getName())
                                .build() : null)
                        .createdByUsername(order.getCreatedBy() != null ? order.getCreatedBy().getUsername() : "System")
                        .status(order.getStatus() != null ? order.getStatus().name() : null)
                        .totalAmount(order.getTotalAmount())
                        .orderDate(order.getOrderDate())
                        .build())
                .collect(Collectors.toList());

        // Top 3 medicines by stock for dashboard display
        List<MedicineResponse> topMedicines = medicineRepository.findTopMedicinesByStock(PageRequest.of(0, 3)).stream()
                .map(m -> MedicineResponse.builder()
                        .id(m.getId())
                        .name(m.getName())
                        .code(m.getCode())
                        .currentStock(m.getInventory() != null ? m.getInventory().getQuantity() : 0)
                        .build())
                .collect(Collectors.toList());

        // Top 5 recent sales for staff dashboard display
        List<SaleResponse> recentSales = saleRepository.findTopSalesWithDetails(PageRequest.of(0, 5)).stream()
                .map(s -> SaleResponse.builder()
                        .id(s.getId())
                        .invoiceNumber(s.getInvoiceNumber())
                        .customerName(s.getCustomerName())
                        .customerPhone(s.getCustomerPhone())
                        .finalAmount(s.getFinalAmount())
                        .totalAmount(s.getTotalAmount())
                        .paymentMethod(s.getPaymentMethod())
                        .saleDate(s.getSaleDate())
                        .createdByUsername(s.getUser() != null ? s.getUser().getUsername() : "N/A")
                        .build())
                .collect(Collectors.toList());

        return DashboardSummaryDto.builder()
                .totalUsers(totalUsers)
                .totalMedicines(totalMedicines)
                .totalSuppliers(totalSuppliers)
                .totalPurchaseOrders(totalOrders)
                .lowStockCount(lowStockMedicinesCount)
                .totalInventoryValue(totalValue)
                .currency("INR")
                .lowStockItems(lowStockItems)
                .recentOrders(recentOrders)
                .availableMedicinesCount(availableCount)
                .lowStockMedicinesCount(lowStockMedicinesCount)
                .outOfStockMedicinesCount(outOfStockCount)
                .nearExpiryMedicinesCount(nearExpiryCount)
                .expiredMedicinesCount(expiredCount)
                .topMedicines(topMedicines)
                .recentSales(recentSales)
                .build();
    }
}
