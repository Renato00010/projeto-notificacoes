package com.unilabs.controller;

import com.unilabs.dto.NotificationCreateResponse;
import com.unilabs.dto.NotificationRequest;
import com.unilabs.dto.NotificationRetryResponse;
import com.unilabs.dto.NotificationStatusResponse;
import com.unilabs.service.NotificationRetryService;
import com.unilabs.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notificações", description = "Submissão e consulta de estado de notificações")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRetryService retryService;

    public NotificationController(NotificationService notificationService,
                                  NotificationRetryService retryService) {
        this.notificationService = notificationService;
        this.retryService = retryService;
    }

    @Operation(
            summary = "Criar notificação",
            description = """
                    Submete uma notificação para processamento assíncrono.
                    O campo `channelType` identifica o canal: EMAIL, SMS ou PUSH.
                    A mensagem é publicada no RabbitMQ e o job retorna com estado PENDING.
                    """,
            responses = {
                    @ApiResponse(responseCode = "202", description = "Notificação aceite para processamento",
                            content = @Content(schema = @Schema(implementation = NotificationCreateResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Payload inválido")
            }
    )
    @PostMapping
    public ResponseEntity<NotificationCreateResponse> createNotification(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Pedido de notificação",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = NotificationRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "E-mail",
                                            value = """
                                                    {
                                                      "clientId": "portal-do-paciente",
                                                      "channelType": "EMAIL",
                                                      "recipient": "paciente@exemplo.com",
                                                      "templateName": "template_resultados_exame",
                                                      "parameters": {"nome_paciente": "Maria Silva"},
                                                      "callbackUrl": "https://meu-sistema.com/webhooks/notificacoes"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "SMS",
                                            value = """
                                                    {
                                                      "clientId": "portal-do-paciente",
                                                      "channelType": "SMS",
                                                      "recipient": "+351912345678",
                                                      "templateName": "template_lembrete_consulta",
                                                      "parameters": {"data_consulta": "2026-06-20"},
                                                      "callbackUrl": "https://meu-sistema.com/webhooks/notificacoes"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Push",
                                            value = """
                                                    {
                                                      "clientId": "app-mobile",
                                                      "channelType": "PUSH",
                                                      "recipient": "fcm-token-exemplo-abc123",
                                                      "templateName": "template_resultado_disponivel",
                                                      "parameters": {"titulo": "Resultado disponível"},
                                                      "callbackUrl": "https://meu-sistema.com/webhooks/notificacoes"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @RequestBody NotificationRequest request) {
        UUID jobId = notificationService.queueNotification(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new NotificationCreateResponse(jobId, "PENDING"));
    }

    @Operation(
            summary = "Consultar estado do job",
            description = "Retorna o estado atual do job e, quando disponível, o canal e destinatário.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Estado encontrado",
                            content = @Content(schema = @Schema(implementation = NotificationStatusResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Job não encontrado")
            }
    )
    @GetMapping("/{jobId}")
    public NotificationStatusResponse getStatus(
            @Parameter(description = "ID do job", example = "f784e1b8-6a31-4cfa-81a1-cf4939ff7b2b")
            @PathVariable UUID jobId) {
        return notificationService.getNotificationStatus(jobId);
    }

    @Operation(
            summary = "Reenviar job falhado",
            description = "Republica na fila RabbitMQ um job com estado FAILED (máximo 3 reenvios).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Job reenviado",
                            content = @Content(schema = @Schema(implementation = NotificationRetryResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Job não elegível para reenvio")
            }
    )
    @PostMapping("/{jobId}/retry")
    public NotificationRetryResponse retry(
            @Parameter(description = "ID do job falhado")
            @PathVariable UUID jobId) {
        return retryService.retry(jobId);
    }
}
