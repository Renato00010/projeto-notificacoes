package com.unilabs.view;

import com.unilabs.client.NotificationApiClient;
import com.unilabs.domain.NotificationJob;
import com.unilabs.domain.NotificationLog;
import com.unilabs.dto.NotificationCountResponse;
import com.unilabs.dto.NotificationFilterResponse;
import com.unilabs.dto.NotificationRequest;
import com.unilabs.dto.NotificationStatsResponse;
import com.unilabs.repository.NotificationJobRepository;
import com.unilabs.repository.NotificationLogRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Route("")
@Transactional
public class NotificationView extends VerticalLayout {

    private final NotificationLogRepository logRepository;
    private final NotificationJobRepository jobRepository;
    private final NotificationApiClient apiClient;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public NotificationView(NotificationLogRepository logRepository,
                            NotificationJobRepository jobRepository,
                            NotificationApiClient apiClient) {
        this.logRepository = logRepository;
        this.jobRepository = jobRepository;
        this.apiClient = apiClient;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.getStyle().set("background-color", "#0f172a");
        header.getStyle().set("border-bottom", "1px solid #1e293b");

        H2 title = new H2("Unilabs Notification Center");
        title.getStyle().set("color", "white");
        title.getStyle().set("margin", "0");
        title.getStyle().set("font-size", "1.6rem");
        title.getStyle().set("font-weight", "700");
        header.add(title);
        header.setAlignItems(Alignment.CENTER);
        add(header);

        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.getStyle().set("padding", "20px");

        tabSheet.add("Dashboard", createDashboardTab());
        tabSheet.add("Enviar Notificação", createFormTab());
        tabSheet.add("Consulta por Destinatário", createSearchTab());
        tabSheet.add("Histórico de Jobs", createJobsTab());
        tabSheet.add("Logs de Auditoria", createLogsTab());

        add(tabSheet);
    }

    // ─── Dashboard ────────────────────────────────────────────────────────────

    private VerticalLayout createDashboardTab() {
        VerticalLayout tab = new VerticalLayout();
        tab.setSizeFull();
        tab.setPadding(true);
        tab.setSpacing(true);

        HorizontalLayout metricsLayout = new HorizontalLayout();
        metricsLayout.setWidthFull();

        VerticalLayout infoSection = new VerticalLayout();
        infoSection.addClassName("form-card");
        infoSection.setPadding(true);

        H3 welcome = new H3("Estado Operacional do Motor");
        welcome.addClassName("form-title");

        Span desc = new Span("Métricas obtidas via API REST do backend. O centro de comunicações processa notificações de forma assíncrona usando RabbitMQ.");
        desc.getStyle().set("color", "#475569").set("line-height", "1.6");

        infoSection.add(welcome, desc);

        Runnable refreshMetrics = () -> {
            metricsLayout.removeAll();
            try {
                NotificationStatsResponse stats = apiClient.getStats();
                metricsLayout.add(
                        createCard("Total de Jobs", String.valueOf(stats.getTotalJobs()), "stat-card-total"),
                        createCard("Envios Sucesso", String.valueOf(stats.getSuccessJobs()), "stat-card-success"),
                        createCard("Envios Falhados", String.valueOf(stats.getFailedJobs()), "stat-card-failed"),
                        createCard("Pedidos Pendentes", String.valueOf(stats.getPendingJobs()), "stat-card-pending"),
                        createCard("Logs de Auditoria", String.valueOf(stats.getTotalLogs()), "stat-card-total"),
                        createCard("Webhooks OK", String.valueOf(stats.getWebhookSuccess()), "stat-card-success"),
                        createCard("Webhooks Falhados", String.valueOf(stats.getWebhookFailed()), "stat-card-failed")
                );
            } catch (Exception e) {
                metricsLayout.add(createCard("Erro", "Backend indisponível", "stat-card-failed"));
                Notification.show("Não foi possível obter métricas: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        };

        Button refreshBtn = new Button("Atualizar Métricas", e -> refreshMetrics.run());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        refreshMetrics.run();
        tab.add(refreshBtn, metricsLayout, infoSection);
        return tab;
    }

    private VerticalLayout createCard(String title, String value, String className) {
        VerticalLayout card = new VerticalLayout();
        card.addClassNames("stat-card", className);
        card.setPadding(false);
        card.setSpacing(false);
        card.add(new Span(title) {{ addClassName("stat-label"); }},
                 new Span(value) {{ addClassName("stat-value"); }});
        return card;
    }

    // ─── Enviar Notificação ───────────────────────────────────────────────────

    private VerticalLayout createFormTab() {
        VerticalLayout tab = new VerticalLayout();
        tab.setSizeFull();
        tab.setPadding(true);
        tab.setAlignItems(Alignment.CENTER);

        VerticalLayout formCard = new VerticalLayout();
        formCard.addClassName("form-card");
        formCard.setSpacing(true);

        H3 formTitle = new H3("Enviar Nova Comunicação");
        formTitle.addClassName("form-title");
        formCard.add(formTitle);

        TextField clientIdField = new TextField("ID do Cliente / Origem");
        clientIdField.setPlaceholder("Ex: portal-paciente");
        clientIdField.setWidthFull();

        ComboBox<String> channelTypeCombo = new ComboBox<>("Canal de Notificação");
        channelTypeCombo.setItems("EMAIL", "SMS", "PUSH");
        channelTypeCombo.setPlaceholder("Selecione o canal...");
        channelTypeCombo.setWidthFull();

        ComboBox<String> templateCombo = new ComboBox<>("Template");
        templateCombo.setPlaceholder("Selecione um template...");
        templateCombo.setWidthFull();

        channelTypeCombo.addValueChangeListener(event -> {
            templateCombo.clear();
            if (event.getValue() == null) return;
            try {
                templateCombo.setItems(
                        apiClient.listTemplates(event.getValue()).stream()
                                .map(com.unilabs.dto.NotificationTemplateResponse::getName)
                                .toList()
                );
            } catch (Exception e) {
                Notification.show("Erro ao carregar templates: " + e.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });

        TextField recipientField = new TextField("Destinatário");
        recipientField.setPlaceholder("E-mail, número de telemóvel ou token push");
        recipientField.setWidthFull();

        TextField messageField = new TextField("Texto da Mensagem / Parâmetro");
        messageField.setPlaceholder("Escreva o corpo da notificação...");
        messageField.setWidthFull();

        TextField callbackUrlField = new TextField("URL de Callback / Webhook (opcional)");
        callbackUrlField.setPlaceholder("https://exemplo.com/webhook");
        callbackUrlField.setWidthFull();

        Button sendButton = new Button("Disparar Notificação");
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        sendButton.setWidthFull();
        sendButton.getStyle().set("margin-top", "20px");

        formCard.add(clientIdField, channelTypeCombo, templateCombo, recipientField, messageField, callbackUrlField, sendButton);
        tab.add(formCard);

        sendButton.addClickListener(event -> {
            if (clientIdField.isEmpty() || channelTypeCombo.isEmpty() || recipientField.isEmpty() || messageField.isEmpty()) {
                Notification.show("Erro: Preencha Origem, Canal, Destinatário e Mensagem.", 4000, Notification.Position.MIDDLE);
                return;
            }
            try {
                NotificationRequest request = new NotificationRequest();
                request.setClientId(clientIdField.getValue());
                request.setChannelType(channelTypeCombo.getValue());
                request.setRecipient(recipientField.getValue());
                request.setTemplateName(templateCombo.isEmpty()
                        ? suggestDefaultTemplate(channelTypeCombo.getValue())
                        : templateCombo.getValue());
                if (!callbackUrlField.isEmpty()) request.setCallbackUrl(callbackUrlField.getValue());

                Map<String, Object> params = new HashMap<>();
                params.put("texto_mensagem", messageField.getValue());
                request.setParameters(params);

                UUID jobId = apiClient.createNotification(request);
                Notification.show("Notificação enviada! Job ID: " + jobId, 5000, Notification.Position.TOP_CENTER);
                messageField.clear();
            } catch (Exception e) {
                Notification.show("Erro ao submeter: " + e.getMessage(), 5000, Notification.Position.BOTTOM_START);
            }
        });

        return tab;
    }

    // ─── Consulta por Destinatário ────────────────────────────────────────────

    private VerticalLayout createSearchTab() {
        VerticalLayout tab = new VerticalLayout();
        tab.setSizeFull();
        tab.setPadding(true);
        tab.setSpacing(true);

        VerticalLayout formCard = new VerticalLayout();
        formCard.addClassName("form-card");
        formCard.setWidthFull();

        H3 title = new H3("Consultar Notificações por Destinatário");
        title.addClassName("form-title");

        TextField recipientField = new TextField("Destinatário");
        recipientField.setPlaceholder("Ex: +351912345678 ou paciente@exemplo.com");
        recipientField.setWidthFull();

        ComboBox<String> channelCombo = new ComboBox<>("Canal (opcional)");
        channelCombo.setItems("EMAIL", "SMS", "PUSH");
        channelCombo.setClearButtonVisible(true);
        channelCombo.setWidthFull();

        // Filtros de data
        DateTimePicker fromPicker = new DateTimePicker("Data inicial (opcional)");
        fromPicker.setWidthFull();

        DateTimePicker toPicker = new DateTimePicker("Data final (opcional)");
        toPicker.setWidthFull();

        HorizontalLayout dateRow = new HorizontalLayout(fromPicker, toPicker);
        dateRow.setWidthFull();

        Span countLabel = new Span("Informe um destinatário e clique em Pesquisar.");
        countLabel.getStyle().set("font-size", "1.1rem").set("color", "#334155");

        Grid<NotificationFilterResponse> resultGrid = new Grid<>(NotificationFilterResponse.class, false);
        resultGrid.setSizeFull();
        resultGrid.addColumn(NotificationFilterResponse::getChannelType).setHeader("Canal").setSortable(true);
        resultGrid.addColumn(NotificationFilterResponse::getRecipient).setHeader("Destinatário").setSortable(true);
        resultGrid.addColumn(NotificationFilterResponse::getStatus).setHeader("Estado").setSortable(true);
        resultGrid.addColumn(NotificationFilterResponse::getProvider).setHeader("Fornecedor");
        resultGrid.addColumn(item -> item.getCreatedAt() != null ? item.getCreatedAt().format(DATE_FORMATTER) : "-")
                .setHeader("Data").setSortable(true);
        resultGrid.addColumn(item -> item.getJobId() != null ? item.getJobId().toString().substring(0, 8) + "..." : "-")
                .setHeader("Job ID");

        Button searchBtn = new Button("Pesquisar", e -> {
            String recipient = recipientField.isEmpty() ? null : recipientField.getValue();
            String channel = channelCombo.isEmpty() ? null : channelCombo.getValue();
            LocalDateTime from = fromPicker.getValue();
            LocalDateTime to = toPicker.getValue();

            if (recipient == null && channel == null && from == null && to == null) {
                Notification.show("Informe pelo menos um filtro.", 4000, Notification.Position.MIDDLE);
                return;
            }
            if (from != null && to != null && from.isAfter(to)) {
                Notification.show("A data inicial não pode ser posterior à data final.", 4000, Notification.Position.MIDDLE);
                return;
            }
            try {
                NotificationCountResponse count = apiClient.countNotifications(recipient, channel, null, null, from, to);
                countLabel.setText("Total encontrado: " + count.getCount() + " notificação(ões)");

                List<NotificationFilterResponse> results = apiClient.searchNotifications(recipient, channel, null, null, from, to);
                resultGrid.setItems(results);
            } catch (Exception ex) {
                Notification.show("Erro na consulta: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });
        searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button clearBtn = new Button("Limpar", e -> {
            recipientField.clear();
            channelCombo.clear();
            fromPicker.setValue(null);
            toPicker.setValue(null);
            resultGrid.setItems(List.of());
            countLabel.setText("Informe um destinatário e clique em Pesquisar.");
        });

        HorizontalLayout btnRow = new HorizontalLayout(searchBtn, clearBtn);

        formCard.add(title, recipientField, channelCombo, dateRow, btnRow, countLabel);
        tab.add(formCard, resultGrid);
        tab.setFlexGrow(1, resultGrid);
        return tab;
    }

    // ─── Histórico de Jobs ────────────────────────────────────────────────────

    private HorizontalLayout createJobsTab() {
        HorizontalLayout tab = new HorizontalLayout();
        tab.setSizeFull();
        tab.setPadding(true);
        tab.setSpacing(true);

        Grid<NotificationJob> jobGrid = new Grid<>(NotificationJob.class, false);
        jobGrid.setSizeFull();

        jobGrid.addColumn(job -> job.getId().toString().substring(0, 8) + "...").setHeader("Job ID");
        jobGrid.addColumn(NotificationJob::getClientId).setHeader("Origem / Cliente").setSortable(true);
        jobGrid.addColumn(NotificationJob::getChannelType).setHeader("Canal").setSortable(true);
        jobGrid.addColumn(NotificationJob::getRecipient).setHeader("Destinatário").setSortable(true);

        jobGrid.addColumn(new ComponentRenderer<>(job -> {
            Span badge = new Span(job.getStatus());
            badge.addClassName("badge");
            if ("SUCCESS".equals(job.getStatus())) badge.addClassName("badge-success");
            else if ("FAILED".equals(job.getStatus())) badge.addClassName("badge-failed");
            else badge.addClassName("badge-pending");
            return badge;
        })).setHeader("Estado").setSortable(true);

        jobGrid.addColumn(job -> job.getCreatedAt().format(DATE_FORMATTER)).setHeader("Criado Em").setSortable(true);

        jobGrid.addColumn(new ComponentRenderer<>(job -> {
            String status = job.getWebhookStatus() != null ? job.getWebhookStatus() : "NONE";
            Span badge = new Span(status);
            badge.addClassName("badge");
            if ("SUCCESS".equals(status)) badge.addClassName("badge-success");
            else if ("FAILED".equals(status)) badge.addClassName("badge-failed");
            else if ("PENDING".equals(status)) badge.addClassName("badge-pending");
            else badge.addClassName("badge-none");
            return badge;
        })).setHeader("Webhook Callback").setSortable(true);

        VerticalLayout detailPanel = new VerticalLayout();
        detailPanel.addClassName("detail-panel");
        detailPanel.setWidth("450px");
        detailPanel.setHeightFull();
        detailPanel.setVisible(false);

        ComboBox<String> statusFilter = new ComboBox<>("Filtrar por estado");
        statusFilter.setItems("TODOS", "PENDING", "SUCCESS", "FAILED");
        statusFilter.setValue("TODOS");
        statusFilter.setWidth("220px");

        Runnable loadJobs = () -> {
            String filter = statusFilter.getValue();
            List<NotificationJob> jobs;
            if ("TODOS".equals(filter)) jobs = jobRepository.findAll();
            else jobs = jobRepository.findByStatus(filter);
            jobGrid.setItems(jobs);
            jobGrid.getDataProvider().refreshAll();
            detailPanel.setVisible(false);
        };

        jobGrid.asSingleSelect().addValueChangeListener(event -> {
            NotificationJob selectedJob = event.getValue();
            if (selectedJob == null) detailPanel.setVisible(false);
            else showJobDetails(selectedJob, detailPanel, jobGrid, loadJobs);
        });

        statusFilter.addValueChangeListener(e -> loadJobs.run());
        loadJobs.run();

        Button refreshBtn = new Button("Atualizar Lista", e -> loadJobs.run());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        HorizontalLayout toolbar = new HorizontalLayout(statusFilter, refreshBtn);
        toolbar.setAlignItems(Alignment.END);
        toolbar.setWidthFull();

        VerticalLayout gridLayout = new VerticalLayout(toolbar, jobGrid);
        gridLayout.setSizeFull();
        gridLayout.setPadding(false);

        tab.add(gridLayout, detailPanel);
        tab.setFlexGrow(1, gridLayout);
        return tab;
    }

    private void showJobDetails(NotificationJob job, VerticalLayout panel,
                                Grid<NotificationJob> jobGrid, Runnable reloadJobs) {
        panel.removeAll();
        panel.setVisible(true);

        H3 title = new H3("Detalhes do Pedido");
        title.getStyle().set("margin-top", "0").set("border-bottom", "2px solid #e2e8f0").set("padding-bottom", "8px");
        panel.add(title);

        VerticalLayout generalSec = new VerticalLayout();
        generalSec.addClassName("detail-section");
        generalSec.setPadding(false);
        generalSec.add(
                createDetailItem("ID Único", job.getId().toString()),
                createDetailItem("Cliente Origem", job.getClientId()),
                createDetailItem("Canal", job.getChannelType() != null ? job.getChannelType() : "N/A"),
                createDetailItem("Destinatário", job.getRecipient() != null ? job.getRecipient() : "N/A"),
                createDetailItem("Template", job.getTemplateName() != null ? job.getTemplateName() : "N/A"),
                createDetailItem("Estado de Execução", job.getStatus()),
                createDetailItem("Reenvios", String.valueOf(job.getRetryCount())),
                createDetailItem("Data Pedido", job.getCreatedAt().format(DATE_FORMATTER))
        );
        panel.add(generalSec);

        VerticalLayout webhookSec = new VerticalLayout();
        webhookSec.addClassName("detail-section");
        webhookSec.setPadding(false);
        webhookSec.add(
                createDetailItem("Estado Webhook Callback", job.getWebhookStatus() != null ? job.getWebhookStatus() : "NONE"),
                createDetailItem("Log de Resposta Webhook", job.getWebhookResponse() != null ? job.getWebhookResponse() : "N/A")
        );
        panel.add(webhookSec);

        List<NotificationLog> logs = logRepository.findByJobId(job.getId());
        if (!logs.isEmpty()) {
            NotificationLog log = logs.get(0);
            VerticalLayout logSec = new VerticalLayout();
            logSec.addClassName("detail-section");
            logSec.setPadding(false);
            logSec.add(
                    createDetailItem("Canal de Envio", log.getChannelType()),
                    createDetailItem("Fornecedor / Gateway", log.getProvider()),
                    createDetailItem("Destinatário Final", log.getRecipient()),
                    createDetailItem("Payload Parâmetros (JSONB)", log.getPayload() != null ? log.getPayload().toString() : "{}")
            );
            if (log.getErrorMessage() != null) {
                logSec.add(createDetailItem("Mensagem de Erro do Motor", log.getErrorMessage()));
            }
            panel.add(logSec);
        } else {
            Span noLogs = new Span("Aguardando processamento na fila RabbitMQ...");
            noLogs.getStyle().set("font-style", "italic").set("color", "#64748b");
            panel.add(noLogs);
        }

        if ("FAILED".equals(job.getStatus()) && job.getRetryCount() < 3) {
            Button retryBtn = new Button("Reenviar Job", e -> {
                try {
                    apiClient.retryJob(job.getId());
                    Notification.show("Job reenviado para processamento.", 4000, Notification.Position.TOP_CENTER);
                    reloadJobs.run();
                    jobGrid.deselectAll();
                } catch (Exception ex) {
                    Notification.show("Erro ao reenviar: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
                }
            });
            retryBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
            retryBtn.setWidthFull();
            panel.add(retryBtn);
        }
    }

    // ─── Logs de Auditoria ────────────────────────────────────────────────────

    private VerticalLayout createLogsTab() {
        VerticalLayout tab = new VerticalLayout();
        tab.setSizeFull();
        tab.setPadding(true);
        tab.setSpacing(true);

        TextField recipientFilter = new TextField("Destinatário");
        recipientFilter.setPlaceholder("Filtrar por e-mail ou telefone");
        recipientFilter.setClearButtonVisible(true);

        ComboBox<String> channelFilter = new ComboBox<>("Canal");
        channelFilter.setItems("EMAIL", "SMS", "PUSH");
        channelFilter.setClearButtonVisible(true);

        ComboBox<String> statusFilter = new ComboBox<>("Estado");
        statusFilter.setItems("PENDING", "SUCCESS", "FAILED");
        statusFilter.setClearButtonVisible(true);

        // Filtros de data
        DateTimePicker fromPicker = new DateTimePicker("Data inicial");
        DateTimePicker toPicker = new DateTimePicker("Data final");

        HorizontalLayout dateRow = new HorizontalLayout(fromPicker, toPicker);
        dateRow.setWidthFull();

        Grid<NotificationFilterResponse> grid = new Grid<>(NotificationFilterResponse.class, false);
        grid.setSizeFull();
        grid.addColumn(NotificationFilterResponse::getLogId).setHeader("Log ID").setSortable(true);
        grid.addColumn(item -> item.getJobId() != null ? item.getJobId().toString().substring(0, 8) + "..." : "-")
                .setHeader("Job ID");
        grid.addColumn(NotificationFilterResponse::getChannelType).setHeader("Canal").setSortable(true);
        grid.addColumn(NotificationFilterResponse::getProvider).setHeader("Fornecedor").setSortable(true);
        grid.addColumn(NotificationFilterResponse::getRecipient).setHeader("Destinatário").setSortable(true);
        grid.addColumn(item -> item.getParameters() != null ? item.getParameters().toString() : "{}").setHeader("Payload");
        grid.addColumn(NotificationFilterResponse::getErrorMessage).setHeader("Mensagem de Erro");
        grid.addColumn(item -> item.getCreatedAt() != null ? item.getCreatedAt().format(DATE_FORMATTER) : "-")
                .setHeader("Data Log").setSortable(true);

        Runnable loadLogs = () -> {
            String recipient = recipientFilter.isEmpty() ? null : recipientFilter.getValue();
            String channel = channelFilter.isEmpty() ? null : channelFilter.getValue();
            String status = statusFilter.isEmpty() ? null : statusFilter.getValue();
            LocalDateTime from = fromPicker.getValue();
            LocalDateTime to = toPicker.getValue();

            try {
                if (recipient == null && channel == null && status == null && from == null && to == null) {
                    grid.setItems(logRepository.findAll().stream().map(this::toFilterResponse).toList());
                } else {
                    grid.setItems(apiClient.searchNotifications(recipient, channel, null, status, from, to));
                }
            } catch (Exception e) {
                Notification.show("Erro ao filtrar logs: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        };

        Button filterBtn = new Button("Aplicar Filtros", e -> loadLogs.run());
        filterBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button refreshBtn = new Button("Mostrar Todos", e -> {
            recipientFilter.clear();
            channelFilter.clear();
            statusFilter.clear();
            fromPicker.setValue(null);
            toPicker.setValue(null);
            grid.setItems(logRepository.findAll().stream().map(this::toFilterResponse).toList());
        });
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        HorizontalLayout toolbar = new HorizontalLayout(recipientFilter, channelFilter, statusFilter, filterBtn, refreshBtn);
        toolbar.setAlignItems(Alignment.END);
        toolbar.setWidthFull();

        loadLogs.run();
        tab.add(toolbar, dateRow, grid);
        tab.setFlexGrow(1, grid);
        return tab;
    }

    // ─── Utilitários ─────────────────────────────────────────────────────────

    private String suggestDefaultTemplate(String channel) {
        return switch (channel) {
            case "SMS" -> "template_portal_sms";
            case "PUSH" -> "template_portal_push";
            default -> "template_portal";
        };
    }

    private VerticalLayout createDetailItem(String label, String value) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getStyle().set("margin-bottom", "10px");
        Span lbl = new Span(label);
        lbl.addClassName("detail-label");
        Span val = new Span(value);
        val.addClassName("detail-value");
        layout.add(lbl, val);
        return layout;
    }

    private NotificationFilterResponse toFilterResponse(NotificationLog log) {
        NotificationFilterResponse response = new NotificationFilterResponse();
        response.setLogId(log.getId());
        response.setJobId(log.getJobId());
        response.setChannelType(log.getChannelType());
        response.setProvider(log.getProvider());
        response.setRecipient(log.getRecipient());
        response.setParameters(log.getPayload());
        response.setErrorMessage(log.getErrorMessage());
        response.setCreatedAt(log.getCreatedAt());
        return response;
    }
}
