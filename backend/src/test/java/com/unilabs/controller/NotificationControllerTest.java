package com.unilabs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unilabs.dto.ChannelType;
import com.unilabs.dto.NotificationRequest;
import com.unilabs.service.NotificationRetryService;
import com.unilabs.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationRetryService retryService;

    @InjectMocks
    private NotificationController notificationController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void testCreateNotification_Accepted() throws Exception {
        UUID mockJobId = UUID.randomUUID();
        when(notificationService.queueNotification(any(NotificationRequest.class))).thenReturn(mockJobId);

        NotificationRequest request = new NotificationRequest();
        request.setClientId("test");
        request.setChannelType(ChannelType.EMAIL);
        request.setRecipient("test@test.com");
        request.setCallbackUrl("http://callback.url");

        mockMvc.perform(post("/api/v1/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value(mockJobId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    public void testCreateNotification_BadRequest() throws Exception {
        when(notificationService.queueNotification(any(NotificationRequest.class)))
                .thenThrow(new IllegalArgumentException("O campo 'clientId' e obrigatorio."));

        NotificationRequest request = new NotificationRequest();

        mockMvc.perform(post("/api/v1/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("O campo 'clientId' e obrigatorio."));
    }
}
