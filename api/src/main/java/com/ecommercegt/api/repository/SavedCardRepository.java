package com.ecommercegt.api.repository;

import com.ecommercegt.api.model.SavedCard;
import com.ecommercegt.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SavedCardRepository extends JpaRepository<SavedCard, Long> {
    List<SavedCard> findAllByOwnerOrderByCreatedAtDesc(User owner);
}
