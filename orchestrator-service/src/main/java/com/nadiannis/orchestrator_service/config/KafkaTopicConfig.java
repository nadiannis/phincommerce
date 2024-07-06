package com.nadiannis.orchestrator_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic OrchestratorTopic() {
        return TopicBuilder.name("orchestrator").build();
    }

    @Bean
    public NewTopic OrderTopic() {
        return TopicBuilder.name("order").build();
    }

}
