package com.example.etl.repository;

import com.example.etl.model.EtlHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EtlHistoryRepository extends JpaRepository<EtlHistory, Long> {
    Optional<EtlHistory> findFirstByOrderByStartTimeDesc();
    Optional<EtlHistory> findFirstByStatusOrderByStartTimeDesc(String status);
}
