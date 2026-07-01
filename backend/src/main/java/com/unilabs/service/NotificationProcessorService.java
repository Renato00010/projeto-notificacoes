package com.unilabs.service;

import com.unilabs.domain.NotificationLog;
import com.unilabs.dto.NotificationRequest;
import com.unilabs.provider.NotificationDeliveryException;
import com.unilabs.provider.NotificationProviderRegistry;
import com.unilabs.repository.NotificationJobRepository;
import com.unilabs.repository.NotificationLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class NotificationProcessorService {

    private final NotificationLogRepository logRepository;
    private final NotificationJobRepository jobRepository;
    private final NotificationTemplateService templateService;
    private final NotificationProviderRegistry providerRegistry;
    private final CallbackService callbackService;

    public NotificationProcessorService(NotificationLogRepository logRepository,
                                        NotificationJobRepository jobRepository,
                                        NotificationTemplateService templateService,
                                        NotificationProviderRegistry providerRegistry,
                                        CallbackService callbackService) {
        this.logRepository = logRepository;
        this.jobRepository = jobRepository;
        this.templateService = templateService;
        this.providerRegistry = providerRegistry;
        this.callbackService = callbackService;
    }

    @Transactional
    public void process(NotificationRequest request) {
        UUID jobId = request.getJobId() != null ? request.getJobId() : UUID.randomUUID();
        String status = "SUCCESS";
        String errorMessage = null;
        NotificationTemplateService.RenderedNotification rendered = null;

        NotificationLog log = new NotificationLog();
        log.setJobId(jobId);
        log.setChannelType(request.getChannelType().name());
        log.setRecipient(request.getRecipient());
        log.setCreatedAt(LocalDateTime.now());

        try {
            rendered = templateService.render(
                    request.getTemplateName(),
                    request.getChannelType(),
                    request.getParameters()
            );
            providerRegistry.deliver(request, rendered.subject(), rendered.body());
            log.setProvider(providerRegistry.resolveProviderName(request.getChannelType()));
            log.setPayload(rendered.toPayloadMap(request.getParameters()));
        } catch (NotificationDeliveryException | IllegalArgumentException e) {
            status = "FAILED";
            errorMessage = e.getMessage();
            log.setProvider(providerRegistry.resolveProviderName(request.getChannelType()));
            log.setPayload(rendered != null
                    ? rendered.toPayloadMap(request.getParameters())
                    : (request.getParameters() != null ? request.getParameters() : java.util.Map.of()));
        }

        log.setErrorMessage(errorMessage);
        logRepository.save(log);
        updateJobStatus(jobId, status);
        callbackService.sendCallback(request, status, errorMessage);
    }

    private void updateJobStatus(UUID jobId, String status) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus(status);
            job.setUpdatedAt(LocalDateTime.now());
            jobRepository.save(job);
        });
    }
}
