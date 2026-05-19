package com.example.etl.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "etl_sync_state")
@Data
public class EtlSyncState {
    @Id
    private String tableName;
    
    private LocalDateTime lastSyncTime;
}
