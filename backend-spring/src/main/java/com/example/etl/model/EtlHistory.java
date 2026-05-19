package com.example.etl.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "etl_history")
@Data
public class EtlHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mode; // BATCH / REALTIME
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private String status; // RUNNING / SUCCESS / FAIL
    
    private Integer migratedCount;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
}
