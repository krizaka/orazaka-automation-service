package com.krizaka.orazaka.automationservice.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

/**
 * The asset keyring's path is declared in this service's own {@code application.yml}: the property's built-in default
 * ({@code ${user.home}/…}) is not placeholder-resolved by the binder, so without it the service dies at startup on a
 * deployment that keeps the keyring in the home directory (dev: « master keyring not found at ${user.home}/… »).
 */
class KeyringPathConfigTest {

  private static final String PROPERTY = "orazaka.assets.encryption.master-key-file";

  @Test
  @DisplayName("the keyring path is a resolved path under the user's home, not a literal placeholder")
  void keyringPathIsResolved() throws IOException {
    List<PropertySource<?>> sources =
        new YamlPropertySourceLoader().load("application", new ClassPathResource("application.yml"));
    StandardEnvironment environment = new StandardEnvironment();
    sources.forEach(environment.getPropertySources()::addLast);

    String raw = (String) sources.get(0).getProperty(PROPERTY);

    assertThat(raw).as("declared in application.yml").isNotNull();
    assertThat(environment.resolvePlaceholders(raw))
        .startsWith(System.getProperty("user.home"))
        .doesNotContain("${");
  }
}
