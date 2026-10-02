package com.opsflow.backend.repository;

import com.opsflow.backend.entity.OperationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OperationHistoryRepository
        extends JpaRepository<OperationHistory, Long> {

    List<OperationHistory> findByOperationIdOrderByCreatedAtDesc(
            Long operationId
    );

    Page<OperationHistory> findAllByOrderByCreatedAtDesc(
            Pageable pageable
    );
}