package com.opsflow.backend.service;

import com.opsflow.backend.entity.OperationHistory;
import com.opsflow.backend.repository.OperationHistoryRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperationHistoryService {

    private final OperationHistoryRepository repository;

    public OperationHistoryService(
            OperationHistoryRepository repository) {

        this.repository = repository;
    }

    public Page<OperationHistory> getHistory(
            Pageable pageable) {

        return repository
                .findAllByOrderByCreatedAtDesc(pageable);
    }

    public List<OperationHistory> getHistoryByOperationId(
            Long operationId) {

        return repository
                .findByOperationIdOrderByCreatedAtDesc(
                        operationId
                );
    }
}