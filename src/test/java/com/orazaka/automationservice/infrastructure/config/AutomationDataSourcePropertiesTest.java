package com.orazaka.automationservice.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AutomationDataSourcePropertiesTest {

  @Test
  @DisplayName("Valid automation datasource wiring is accepted")
  void validWiring() {
    var properties =
        new AutomationDataSourceProperties(
            "jdbc:postgresql://localhost:5432/orazaka_automation_db",
            "orazaka_automation",
            "secret");
    assertThat(properties.url()).contains("orazaka_automation_db");
    assertThat(properties.username()).isEqualTo("orazaka_automation");
  }

  @Test
  @DisplayName("Missing values and non-postgres URLs are rejected")
  void invalidWiringRejected() {
    assertThatNullPointerException()
        .isThrownBy(() -> new AutomationDataSourceProperties(null, "u", "p"));
    assertThatNullPointerException()
        .isThrownBy(
            () ->
                new AutomationDataSourceProperties(
                    "jdbc:postgresql://localhost:5432/db", null, "p"));
    assertThatNullPointerException()
        .isThrownBy(
            () ->
                new AutomationDataSourceProperties(
                    "jdbc:postgresql://localhost:5432/db", "u", null));
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new AutomationDataSourceProperties("jdbc:mysql://x/db", "u", "p"));
  }
}
