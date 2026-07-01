package com.unilabs.service;

import com.unilabs.domain.NotificationJob;
import com.unilabs.domain.NotificationLog;
import com.unilabs.dto.ChannelType;
import com.unilabs.dto.NotificationCountResponse;
import com.unilabs.dto.NotificationFilterResponse;
import com.unilabs.repository.NotificationJobRepository;
import com.unilabs.repository.NotificationLogRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NotificationFilterService {

    private final NotificationLogRepository logRepository;
    private final NotificationJobRepository jobRepository;

    public NotificationFilterService(NotificationLogRepository logRepository,
                                     NotificationJobRepository jobRepository) {
        this.logRepository = logRepository;
        this.jobRepository = jobRepository;
    }

    public List<NotificationFilterResponse> search(String recipient,
                                                   String channelType,
                                                   String clientId,
                                                   String status,
                                                   LocalDateTime from,
                                                   LocalDateTime to) {
        validateFilters(recipient, channelType, clientId, status, from, to);

        List<NotificationLog> logs = logRepository.findAll(
                buildSpecification(recipient, channelType, clientId, status, from, to),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Map<UUID, NotificationJob> jobsById = loadJobs(logs);

        return logs.stream()
                .map(log -> toFilterResponse(log, jobsById.get(log.getJobId())))
                .toList();
    }

    public NotificationCountResponse count(String recipient,
                                           String channelType,
                                           String clientId,
                                           String status,
                                           LocalDateTime from,
                                           LocalDateTime to) {
        validateFilters(recipient, channelType, clientId, status, from, to);

        long total = logRepository.count(buildSpecification(recipient, channelType, clientId, status, from, to));
        String normalizedChannel = (channelType != null && !channelType.isBlank()) ? normalizeChannelType(channelType) : null;
        return new NotificationCountResponse(total, recipient, normalizedChannel);
    }

    private void validateFilters(String recipient,
                                 String channelType,
                                 String clientId,
                                 String status,
                                 LocalDateTime from,
                                 LocalDateTime to) {
        boolean hasRecipient = recipient != null && !recipient.isBlank();
        boolean hasClientId = clientId != null && !clientId.isBlank();
        boolean hasChannelType = channelType != null && !channelType.isBlank();
        boolean hasStatus = status != null && !status.isBlank();
        boolean hasDateRange = from != null || to != null;

        if (!hasRecipient && !hasClientId && !hasChannelType && !hasStatus && !hasDateRange) {
            throw new IllegalArgumentException(
                    "Informe pelo menos um filtro: recipient, clientId, channelType, status, from ou to."
            );
        }

        if (channelType != null && !channelType.isBlank()) {
            ChannelType.fromValue(channelType);
        }
    }

    private Specification<NotificationLog> buildSpecification(String recipient,
                                                                String channelType,
                                                                String clientId,
                                                                String status,
                                                                LocalDateTime from,
                                                                LocalDateTime to) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (recipient != null && !recipient.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("recipient"), recipient.trim()));
            }

            if (channelType != null && !channelType.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("channelType"), normalizeChannelType(channelType)));
            }

            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from));
            }

            if (to != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), to));
            }

            if ((clientId != null && !clientId.isBlank()) || (status != null && !status.isBlank())) {
                Subquery<UUID> jobSubquery = query.subquery(UUID.class);
                Root<NotificationJob> jobRoot = jobSubquery.from(NotificationJob.class);
                jobSubquery.select(jobRoot.get("id"));

                List<Predicate> jobPredicates = new ArrayList<>();
                if (clientId != null && !clientId.isBlank()) {
                    jobPredicates.add(criteriaBuilder.equal(jobRoot.get("clientId"), clientId.trim()));
                }
                if (status != null && !status.isBlank()) {
                    jobPredicates.add(criteriaBuilder.equal(jobRoot.get("status"), status.trim().toUpperCase()));
                }
                jobSubquery.where(criteriaBuilder.and(jobPredicates.toArray(new Predicate[0])));
                predicates.add(root.get("jobId").in(jobSubquery));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Map<UUID, NotificationJob> loadJobs(List<NotificationLog> logs) {
        List<UUID> jobIds = logs.stream().map(NotificationLog::getJobId).distinct().toList();
        return jobRepository.findAllById(jobIds).stream()
                .collect(Collectors.toMap(NotificationJob::getId, Function.identity()));
    }

    private NotificationFilterResponse toFilterResponse(NotificationLog log, NotificationJob job) {
        NotificationFilterResponse response = new NotificationFilterResponse();
        response.setLogId(log.getId());
        response.setJobId(log.getJobId());
        response.setChannelType(log.getChannelType());
        response.setRecipient(log.getRecipient());
        response.setProvider(log.getProvider());
        response.setParameters(log.getPayload());
        response.setErrorMessage(log.getErrorMessage());
        response.setCreatedAt(log.getCreatedAt());

        if (job != null) {
            response.setClientId(job.getClientId());
            response.setStatus(job.getStatus());
        }

        return response;
    }

    private String normalizeChannelType(String channelType) {
        return ChannelType.fromValue(channelType).name();
    }
}
