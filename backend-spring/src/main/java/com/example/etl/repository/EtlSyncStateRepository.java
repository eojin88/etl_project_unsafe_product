package com.example.etl.repository;

import com.example.etl.model.EtlSyncState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EtlSyncStateRepository extends JpaRepository<EtlSyncState, String> {
}
