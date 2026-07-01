package com.unilabs.repository;

import com.unilabs.domain.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    Optional<NotificationTemplate> findByNameAndActiveTrue(String name);
    List<NotificationTemplate> findByChannelTypeAndActiveTrue(String channelType);
    List<NotificationTemplate> findByActiveTrue();
}
