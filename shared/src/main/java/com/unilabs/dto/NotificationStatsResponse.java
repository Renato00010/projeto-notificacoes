package com.unilabs.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Métricas operacionais do Notification Center")
public class NotificationStatsResponse {

    @Schema(description = "Total de jobs registados", example = "150")
    private long totalJobs;

    @Schema(description = "Jobs com envio concluído com sucesso", example = "120")
    private long successJobs;

    @Schema(description = "Jobs com falha no processamento", example = "10")
    private long failedJobs;

    @Schema(description = "Jobs aguardando processamento", example = "20")
    private long pendingJobs;

    @Schema(description = "Total de logs de auditoria", example = "130")
    private long totalLogs;

    @Schema(description = "Callbacks webhook concluídos com sucesso", example = "100")
    private long webhookSuccess;

    @Schema(description = "Callbacks webhook com falha", example = "5")
    private long webhookFailed;

    public NotificationStatsResponse() {}

    public NotificationStatsResponse(long totalJobs, long successJobs, long failedJobs, long pendingJobs,
                                   long totalLogs, long webhookSuccess, long webhookFailed) {
        this.totalJobs = totalJobs;
        this.successJobs = successJobs;
        this.failedJobs = failedJobs;
        this.pendingJobs = pendingJobs;
        this.totalLogs = totalLogs;
        this.webhookSuccess = webhookSuccess;
        this.webhookFailed = webhookFailed;
    }

    public long getTotalJobs() { return totalJobs; }
    public void setTotalJobs(long totalJobs) { this.totalJobs = totalJobs; }

    public long getSuccessJobs() { return successJobs; }
    public void setSuccessJobs(long successJobs) { this.successJobs = successJobs; }

    public long getFailedJobs() { return failedJobs; }
    public void setFailedJobs(long failedJobs) { this.failedJobs = failedJobs; }

    public long getPendingJobs() { return pendingJobs; }
    public void setPendingJobs(long pendingJobs) { this.pendingJobs = pendingJobs; }

    public long getTotalLogs() { return totalLogs; }
    public void setTotalLogs(long totalLogs) { this.totalLogs = totalLogs; }

    public long getWebhookSuccess() { return webhookSuccess; }
    public void setWebhookSuccess(long webhookSuccess) { this.webhookSuccess = webhookSuccess; }

    public long getWebhookFailed() { return webhookFailed; }
    public void setWebhookFailed(long webhookFailed) { this.webhookFailed = webhookFailed; }
}
