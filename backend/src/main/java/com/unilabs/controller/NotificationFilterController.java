package com.unilabs.controller;

import com.unilabs.dto.NotificationCountResponse;
import com.unilabs.dto.NotificationFilterResponse;
import com.unilabs.service.NotificationFilterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications/filter")
@Tag(name = "Filtro de Notificações", description = "Consulta e contagem de notificações por destinatário, canal e outros critérios")
public class NotificationFilterController {

    private final NotificationFilterService filterService;

    public NotificationFilterController(NotificationFilterService filterService) {
        this.filterService = filterService;
    }

    @Operation(
            summary = "Listar notificações filtradas",
            description = """
                    Pesquisa notificações nos logs de auditoria. Útil para verificar, por exemplo,
                    quantas notificações foram enviadas para um determinado número de telefone.
                    É necessário informar pelo menos um filtro.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de notificações encontradas",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificationFilterResponse.class)))),
                    @ApiResponse(responseCode = "400", description = "Filtros inválidos ou ausentes")
            }
    )
    @GetMapping
    public List<NotificationFilterResponse> search(
            @Parameter(description = "Destinatário exato (e-mail, telefone ou token push)", example = "+351912345678")
            @RequestParam(required = false) String recipient,
            @Parameter(description = "Canal de envio", example = "SMS", schema = @Schema(allowableValues = {"EMAIL", "SMS", "PUSH"}))
            @RequestParam(required = false) String channelType,
            @Parameter(description = "Sistema de origem", example = "portal-do-paciente")
            @RequestParam(required = false) String clientId,
            @Parameter(description = "Estado do job", example = "SUCCESS", schema = @Schema(allowableValues = {"PENDING", "SUCCESS", "FAILED"}))
            @RequestParam(required = false) String status,
            @Parameter(description = "Data/hora inicial (ISO-8601)", example = "2026-06-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "Data/hora final (ISO-8601)", example = "2026-06-30T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return filterService.search(recipient, channelType, clientId, status, from, to);
    }

    @Operation(
            summary = "Contar notificações filtradas",
            description = "Retorna o total de notificações que correspondem aos filtros aplicados.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Contagem realizada com sucesso",
                            content = @Content(schema = @Schema(implementation = NotificationCountResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Filtros inválidos ou ausentes")
            }
    )
    @GetMapping("/count")
    public NotificationCountResponse count(
            @Parameter(description = "Destinatário exato", example = "+351912345678")
            @RequestParam(required = false) String recipient,
            @Parameter(description = "Canal de envio", example = "SMS")
            @RequestParam(required = false) String channelType,
            @Parameter(description = "Sistema de origem")
            @RequestParam(required = false) String clientId,
            @Parameter(description = "Estado do job", example = "SUCCESS")
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return filterService.count(recipient, channelType, clientId, status, from, to);
    }
}
