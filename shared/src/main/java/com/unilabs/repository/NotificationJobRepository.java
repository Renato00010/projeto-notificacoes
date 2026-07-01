package com.unilabs.repository;

import com.unilabs.domain.NotificationJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationJobRepository extends JpaRepository<NotificationJob, UUID> {
    long countByStatus(String status);
    long countByWebhookStatus(String webhookStatus);
    List<NotificationJob> findByStatus(String status);
}