package com.pennypals.repository;

import com.pennypals.model.Quest;
import com.pennypals.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestRepository extends JpaRepository<Quest, Long> {
    List<Quest> findByUser(User user);
}