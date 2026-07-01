package com.unilabs.controller;

import com.unilabs.dto.NotificationCountResponse;
import com.unilabs.dto.NotificationFilterResponse;
import com.unilabs.service.NotificationFilterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class NotificationFilterControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationFilterService filterService;

    @InjectMocks
    private NotificationFilterController filterController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(filterController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void testSearchByRecipient() throws Exception {
        NotificationFilterResponse item = new NotificationFilterResponse();
        item.setLogId(1L);
        item.setJobId(UUID.randomUUID());
        item.setRecipient("+351912345678");
        item.setChannelType("SMS");
        item.setStatus("SUCCESS");
        item.setCreatedAt(LocalDateTime.now());

        when(filterService.search(eq("+351912345678"), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/notifications/filter")
                        .param("recipient", "+351912345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recipient").value("+351912345678"))
                .andExpect(jsonPath("$[0].channelType").value("SMS"));
    }

    @Test
    public void testCountByRecipientAndChannel() throws Exception {
        when(filterService.count(eq("+351912345678"), eq("SMS"), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(new NotificationCountResponse(5, "+351912345678", "SMS"));

        mockMvc.perform(get("/api/v1/notifications/filter/count")
                        .param("recipient", "+351912345678")
                        .param("channelType", "SMS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5))
                .andExpect(jsonPath("$.recipient").value("+351912345678"))
                .andExpect(jsonPath("$.channelType").value("SMS"));
    }

    @Test
    public void testSearchWithoutFilters_BadRequest() throws Exception {
        when(filterService.search(isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenThrow(new IllegalArgumentException("Informe pelo menos um filtro: recipient, clientId, channelType, status, from ou to."));

        mockMvc.perform(get("/api/v1/notifications/filter"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Informe pelo menos um filtro: recipient, clientId, channelType, status, from ou to."));
    }
}
