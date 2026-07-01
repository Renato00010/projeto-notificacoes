package com.unilabs.service;

import com.unilabs.domain.NotificationTemplate;
import com.unilabs.dto.ChannelType;
import com.unilabs.dto.NotificationTemplateResponse;
import com.unilabs.repository.NotificationTemplateRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationTemplateService {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([a-zA-Z0-9_]+)}}");

    private final NotificationTemplateRepository templateRepository;

    public NotificationTemplateService(NotificationTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public List<NotificationTemplateResponse> listTemplates(String channelType) {
        List<NotificationTemplate> templates = channelType == null || channelType.isBlank()
                ? templateRepository.findByActiveTrue()
                : templateRepository.findByChannelTypeAndActiveTrue(normalizeChannel(channelType));

        return templates.stream()
                .map(template -> new NotificationTemplateResponse(
                        template.getName(),
                        template.getChannelType(),
                        template.getSubject(),
                        truncate(template.getBody(), 120)
                ))
                .toList();
    }

    public RenderedNotification render(String templateName, ChannelType channelType, Map<String, Object> parameters) {
        Map<String, Object> safeParams = parameters != null ? parameters : Map.of();

        if (templateName == null || templateName.isBlank()) {
            String body = safeParams.getOrDefault("texto_mensagem", "").toString();
            return new RenderedNotification("Notificação", body);
        }

        NotificationTemplate template = templateRepository.findByNameAndActiveTrue(templateName)
                .orElseThrow(() -> new IllegalArgumentException("Template '" + templateName + "' não encontrado."));

        if (!template.getChannelType().equals(channelType.name())) {
            throw new IllegalArgumentException(
                    "Template '" + templateName + "' não é compatível com o canal " + channelType.name()
            );
        }

        return new RenderedNotification(
                applyPlaceholders(template.getSubject(), safeParams),
                applyPlaceholders(template.getBody(), safeParams)
        );
    }

    private String applyPlaceholders(String template, Map<String, Object> parameters) {
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = parameters.getOrDefault(key, "{{" + key + "}}");
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(String.valueOf(value)));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String normalizeChannel(String channelType) {
        return ChannelType.fromValue(channelType).name();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }

    public record RenderedNotification(String subject, String body) {

        public Map<String, Object> toPayloadMap(Map<String, Object> originalParameters) {
            Map<String, Object> payload = new LinkedHashMap<>();
            if (originalParameters != null) {
                payload.putAll(originalParameters);
            }
            payload.put("renderedSubject", subject);
            payload.put("renderedBody", body);
            return payload;
        }
    }
}
