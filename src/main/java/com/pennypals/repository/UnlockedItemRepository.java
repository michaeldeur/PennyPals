package com.pennypals.repository;

import com.pennypals.model.UnlockedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnlockedItemRepository extends JpaRepository<UnlockedItem, Long> {
    List<UnlockedItem> findByUserId(Long userId);
}