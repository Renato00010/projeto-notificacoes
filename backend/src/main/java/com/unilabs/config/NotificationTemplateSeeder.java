package com.unilabs.config;

import com.unilabs.domain.NotificationTemplate;
import com.unilabs.repository.NotificationTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationTemplateSeeder {

    private static final Logger log = LoggerFactory.getLogger(NotificationTemplateSeeder.class);

    @Bean
    CommandLineRunner seedTemplates(NotificationTemplateRepository repository) {
        return args -> {
            seedIfMissing(repository, template(
                    "template_resultados_exame", "EMAIL",
                    "Resultados de exame disponíveis",
                    "Olá {{nome_paciente}}, os resultados do exame de {{data_exame}} já estão disponíveis na sua área de paciente."
            ));
            seedIfMissing(repository, template(
                    "template_lembrete_consulta", "SMS",
                    "Lembrete de consulta",
                    "Consulta dia {{data_consulta}} às {{hora_consulta}}. Unilabs."
            ));
            seedIfMissing(repository, template(
                    "template_resultado_disponivel", "PUSH",
                    "{{titulo}}",
                    "{{corpo}}"
            ));
            seedIfMissing(repository, template(
                    "template_portal", "EMAIL",
                    "Mensagem Unilabs",
                    "{{texto_mensagem}}"
            ));
            seedIfMissing(repository, template(
                    "template_portal_sms", "SMS",
                    "Unilabs",
                    "{{texto_mensagem}}"
            ));
            seedIfMissing(repository, template(
                    "template_portal_push", "PUSH",
                    "Unilabs",
                    "{{texto_mensagem}}"
            ));
            log.info("Templates de notificacao verificados/carregados.");
        };
    }

    private void seedIfMissing(NotificationTemplateRepository repository, NotificationTemplate template) {
        if (repository.findByNameAndActiveTrue(template.getName()).isEmpty()) {
            repository.save(template);
            log.info("Template criado: {}", template.getName());
        }
    }

    private NotificationTemplate template(String name, String channel, String subject, String body) {
        NotificationTemplate template = new NotificationTemplate();
        template.setName(name);
        template.setChannelType(channel);
        template.setSubject(subject);
        template.setBody(body);
        template.setActive(true);
        return template;
    }
}
