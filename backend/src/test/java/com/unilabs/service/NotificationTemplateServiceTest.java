package com.unilabs.service;

import com.unilabs.domain.NotificationTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class NotificationTemplateServiceTest {

    @Mock
    private com.unilabs.repository.NotificationTemplateRepository templateRepository;

    @InjectMocks
    private NotificationTemplateService templateService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRender_ReplacesPlaceholders() {
        NotificationTemplate template = new NotificationTemplate();
        template.setName("template_lembrete_consulta");
        template.setChannelType("SMS");
        template.setSubject("Lembrete");
        template.setBody("Consulta dia {{data_consulta}} às {{hora_consulta}}.");

        when(templateRepository.findByNameAndActiveTrue("template_lembrete_consulta"))
                .thenReturn(Optional.of(template));

        NotificationTemplateService.RenderedNotification rendered = templateService.render(
                "template_lembrete_consulta",
                com.unilabs.dto.ChannelType.SMS,
                Map.of("data_consulta", "2026-06-20", "hora_consulta", "10:30")
        );

        assertEquals("Consulta dia 2026-06-20 às 10:30.", rendered.body());
    }

    @Test
    public void testRender_TemplateNotFound() {
        when(templateRepository.findByNameAndActiveTrue("inexistente")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                templateService.render("inexistente", com.unilabs.dto.ChannelType.EMAIL, Map.of())
        );
    }
}
