package com.pennypals.repository;

import com.pennypals.model.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionSpecifications {

    public static Specification<Transaction> belongsToUser(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Transaction> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("category")), pattern),
                    cb.like(cb.lower(root.get("notes")), pattern)
            );
        };
    }

    public static Specification<Transaction> hasType(String type) {
        return (root, query, cb) -> {
            if (type == null || type.trim().isEmpty() || type.equals("ALL")) {
                return cb.conjunction();
            }
            return cb.equal(root.get("type"), type);
        };
    }

    // --- STRETCH GOAL SPECIFICATIONS ---

    public static Specification<Transaction> hasCategory(String category) {
        return (root, query, cb) -> {
            if (category == null || category.trim().isEmpty() || category.equals("ALL")) {
                return cb.conjunction();
            }
            return cb.equal(root.get("category"), category);
        };
    }

    public static Specification<Transaction> inDateRange(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null) {
                return cb.conjunction();
            }
            if (startDate != null && endDate != null) {
                return cb.between(root.get("date"), startDate, endDate);
            }
            if (startDate != null) {
                return cb.greaterThanOrEqualTo(root.get("date"), startDate);
            }
            return cb.lessThanOrEqualTo(root.get("date"), endDate);
        };
    }

    public static Specification<Transaction> inAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return (root, query, cb) -> {
            if (minAmount == null && maxAmount == null) {
                return cb.conjunction();
            }
            if (minAmount != null && maxAmount != null) {
                return cb.between(root.get("amount"), minAmount, maxAmount);
            }
            if (minAmount != null) {
                return cb.greaterThanOrEqualTo(root.get("amount"), minAmount);
            }
            return cb.lessThanOrEqualTo(root.get("amount"), maxAmount);
        };
    }
}