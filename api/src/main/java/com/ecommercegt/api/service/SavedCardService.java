package com.ecommercegt.api.service;

import com.ecommercegt.api.model.SavedCard;
import com.ecommercegt.api.model.User;
import com.ecommercegt.api.repository.SavedCardRepository;
import com.ecommercegt.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedCardService {

    private final SavedCardRepository cards;
    private final UserRepository users;

    @Transactional(readOnly = true)
    public List<SavedCard> listForUser(Long userId) {
        var owner = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return cards.findAllByOwnerOrderByCreatedAtDesc(owner);
    }

    @Transactional
    public SavedCard create(Long userId, SavedCard payload) {
        var owner = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        payload.setId(null);
        payload.setOwner(owner);
        return cards.save(payload);
    }

    @Transactional
    public void delete(Long userId, Long cardId) {
        var owner = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        var c = cards.findById(cardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        if (!c.getOwner().getId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Card does not belong to user");
        }
        cards.delete(c);
    }
}
