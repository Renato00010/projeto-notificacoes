package com.unilabs.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.YearMonth;

@Component
public class NotificationLogPartitionManager {

    private final JdbcTemplate jdbcTemplate;

    public NotificationLogPartitionManager(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureCurrentPartitions() {
        Boolean partitioned = jdbcTemplate.queryForObject(
                "select relkind = 'p' from pg_class where oid = 'notification_logs'::regclass",
                Boolean.class
        );

        if (!Boolean.TRUE.equals(partitioned)) {
            System.err.println("notification_logs is not partitioned; automatic partition creation skipped.");
            return;
        }

        YearMonth currentMonth = YearMonth.now();
        createMonthlyPartition(currentMonth);
        createMonthlyPartition(currentMonth.plusMonths(1));
    }

    @Scheduled(cron = "0 0 0 25 * ?")
    public void ensureNextMonthPartition() {
        try {
            Boolean partitioned = jdbcTemplate.queryForObject(
                    "select relkind = 'p' from pg_class where oid = 'notification_logs'::regclass",
                    Boolean.class
            );
            if (Boolean.TRUE.equals(partitioned)) {
                YearMonth nextMonth = YearMonth.now().plusMonths(1);
                createMonthlyPartition(nextMonth);
                System.out.println("Partição criada via tarefa agendada para: " + nextMonth);
            }
        } catch (Exception e) {
            System.err.println("Erro ao criar partição na tarefa agendada: " + e.getMessage());
        }
    }

    private void createMonthlyPartition(YearMonth month) {
        String partitionName = "notification_logs_" + month.toString().replace("-", "_");
        LocalDate start = month.atDay(1);
        LocalDate end = month.plusMonths(1).atDay(1);

        String sql = String.format(
                "create table if not exists %s partition of notification_logs for values from ('%s') to ('%s')",
                partitionName,
                start,
                end
        );
        jdbcTemplate.execute(sql);
    }
}
