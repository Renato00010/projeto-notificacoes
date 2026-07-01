package com.unilabs.controller;

import com.unilabs.dto.NotificationTemplateResponse;
import com.unilabs.service.NotificationTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/templates")
@Tag(name = "Templates", description = "Catálogo de templates de notificação por canal")
public class NotificationTemplateController {

    private final NotificationTemplateService templateService;

    public NotificationTemplateController(NotificationTemplateService templateService) {
        this.templateService = templateService;
    }

    @Operation(
            summary = "Listar templates disponíveis",
            description = "Retorna templates activos, opcionalmente filtrados por canal.",
            responses = @ApiResponse(responseCode = "200", description = "Lista de templates",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificationTemplateResponse.class))))
    )
    @GetMapping
    public List<NotificationTemplateResponse> listTemplates(
            @Parameter(description = "Filtrar por canal", example = "SMS")
            @RequestParam(required = false) String channelType) {
        return templateService.listTemplates(channelType);
    }
}
