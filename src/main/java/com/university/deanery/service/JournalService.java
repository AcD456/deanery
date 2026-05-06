package com.university.deanery.service;

import com.university.deanery.model.Journal;
import com.university.deanery.repository.JournalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JournalService {

    @Autowired
    private JournalRepository journalRepository;

    @Transactional
    public void log(Integer userId, String action, String entityType, Integer entityId, String oldValue, String newValue) {
        Journal journal = new Journal(userId, action, entityType, entityId, oldValue, newValue);
        journalRepository.save(journal);
    }

    @Transactional
    public void logSimple(Integer userId, String action, String entityType, Integer entityId) {
        log(userId, action, entityType, entityId, null, null);
    }
}