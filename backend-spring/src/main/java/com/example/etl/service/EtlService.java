package com.example.etl.service;

import com.example.etl.model.EtlHistory;
import com.example.etl.repository.EtlHistoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Service
public class EtlService {

    private final EtlHistoryRepository historyRepository;
    private final RestTemplate restTemplate;
    private final TaskScheduler taskScheduler;
    private ScheduledFuture<?> scheduledTask;

    @Value("${python.etl.url}")
    private String pythonEtlUrl;

    public EtlService(EtlHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
        this.restTemplate = new RestTemplate();
        
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.initialize();
        this.taskScheduler = scheduler;
    }

    public void scheduleEtl(String time) {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }

        // Convert "HH:mm" to cron "0 mm HH * * *"
        String[] parts = time.split(":");
        String cron = String.format("0 %s %s * * *", parts[1], parts[0]);
        
        scheduledTask = taskScheduler.schedule(() -> runEtl("batch"), new CronTrigger(cron));
        System.out.println("ETL Scheduled for " + time + " (Cron: " + cron + ")");
    }

    public void cancelSchedule() {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
            System.out.println("ETL Schedule cancelled.");
        }
    }

    public void runEtl(String mode) {
        EtlHistory history = new EtlHistory();
        history.setMode(mode);
        history.setStartTime(LocalDateTime.now());
        history.setStatus("RUNNING");
        history.setMigratedCount(0);
        historyRepository.save(history);

        try {
            // Fetch last successful sync time for real-time mode
            String lastSyncTimeParam = "";
            if ("realtime".equals(mode)) {
                EtlHistory lastSuccess = historyRepository.findFirstByStatusOrderByStartTimeDesc("SUCCESS").orElse(null);
                if (lastSuccess != null && lastSuccess.getEndTime() != null) {
                    lastSyncTimeParam = "&last_sync_time=" + lastSuccess.getEndTime().toString().replace("T", " ");
                }
            }

            // Trigger Python ETL
            String url = pythonEtlUrl + "?mode=" + mode + lastSyncTimeParam;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && "SUCCESS".equals(response.get("status"))) {
                history.setStatus("SUCCESS");
                history.setMigratedCount((Integer) response.get("count"));
            } else {
                history.setStatus("FAIL");
                history.setErrorMessage(response != null ? (String) response.get("error") : "Unknown error");
            }
        } catch (Exception e) {
            history.setStatus("FAIL");
            history.setErrorMessage(e.getMessage());
        } finally {
            history.setEndTime(LocalDateTime.now());
            historyRepository.save(history);
        }
    }

    public EtlHistory getLatestHistory() {
        return historyRepository.findFirstByOrderByStartTimeDesc().orElse(null);
    }
}
