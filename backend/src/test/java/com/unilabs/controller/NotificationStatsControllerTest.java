package com.unilabs.controller;

import com.unilabs.dto.NotificationStatsResponse;
import com.unilabs.service.NotificationStatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class NotificationStatsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationStatsService statsService;

    @InjectMocks
    private NotificationStatsController statsController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(statsController).build();
    }

    @Test
    public void testGetStats() throws Exception {
        when(statsService.getStats()).thenReturn(
                new NotificationStatsResponse(10, 7, 1, 2, 8, 5, 1)
        );

        mockMvc.perform(get("/api/v1/notifications/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalJobs").value(10))
                .andExpect(jsonPath("$.successJobs").value(7))
                .andExpect(jsonPath("$.pendingJobs").value(2))
                .andExpect(jsonPath("$.totalLogs").value(8));
    }
}
