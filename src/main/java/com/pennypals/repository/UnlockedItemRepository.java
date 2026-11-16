package com.pennypals.repository;

import com.pennypals.model.UnlockedItem;
import com.pennypals.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnlockedItemRepository extends JpaRepository<UnlockedItem, Long> {
    List<UnlockedItem> findByUserId(Long userId);

    @Query("SELECT u FROM UnlockedItem u WHERE u.user = :user AND u.isEquipped = :isEquipped")
    List<UnlockedItem> findByUserAndIsEquipped(@Param("user") User user, @Param("isEquipped") Boolean isEquipped);
}