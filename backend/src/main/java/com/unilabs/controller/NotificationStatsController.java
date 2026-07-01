package com.unilabs.controller;

import com.unilabs.dto.NotificationStatsResponse;
import com.unilabs.service.NotificationStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications/stats")
@Tag(name = "Estatísticas", description = "Métricas operacionais do motor de notificações")
public class NotificationStatsController {

    private final NotificationStatsService statsService;

    public NotificationStatsController(NotificationStatsService statsService) {
        this.statsService = statsService;
    }

    @Operation(
            summary = "Obter métricas operacionais",
            description = "Retorna contagens agregadas de jobs, logs e callbacks webhook.",
            responses = @ApiResponse(responseCode = "200", description = "Métricas obtidas com sucesso",
                    content = @Content(schema = @Schema(implementation = NotificationStatsResponse.class)))
    )
    @GetMapping
    public NotificationStatsResponse getStats() {
        return statsService.getStats();
    }
}
