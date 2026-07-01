package com.unilabs.service;

import com.unilabs.dto.NotificationStatsResponse;
import com.unilabs.repository.NotificationJobRepository;
import com.unilabs.repository.NotificationLogRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificationStatsService {

    private final NotificationJobRepository jobRepository;
    private final NotificationLogRepository logRepository;

    public NotificationStatsService(NotificationJobRepository jobRepository,
                                    NotificationLogRepository logRepository) {
        this.jobRepository = jobRepository;
        this.logRepository = logRepository;
    }

    public NotificationStatsResponse getStats() {
        return new NotificationStatsResponse(
                jobRepository.count(),
                jobRepository.countByStatus("SUCCESS"),
                jobRepository.countByStatus("FAILED"),
                jobRepository.countByStatus("PENDING"),
                logRepository.count(),
                jobRepository.countByWebhookStatus("SUCCESS"),
                jobRepository.countByWebhookStatus("FAILED")
        );
    }
}
