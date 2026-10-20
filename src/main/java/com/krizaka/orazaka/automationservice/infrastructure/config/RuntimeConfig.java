package com.krizaka.orazaka.automationservice.infrastructure.config;

import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The clock every time-dependent decision reads (injected, so tests can fix it), and the wiring.
 */
@Configuration
@EnableConfigurationProperties(PublicUrlProperties.class)
class RuntimeConfig {

  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }
}
