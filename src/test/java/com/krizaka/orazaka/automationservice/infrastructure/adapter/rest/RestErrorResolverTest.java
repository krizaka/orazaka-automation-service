package com.krizaka.orazaka.automationservice.infrastructure.adapter.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.krizaka.orazaka.automationservice.domain.exception.AutomationNotFoundException;
import com.krizaka.orazaka.automationservice.domain.exception.TooManyAutomationsException;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

class RestErrorResolverTest {

  private final RestErrorResolver resolver = new RestErrorResolver();

  @Test
  @DisplayName("a missing automation is a 404 that does not say whose it is")
  void notFound() {
    ProblemDetail problem = resolver.notFound(new AutomationNotFoundException(UUID.randomUUID()));
    assertThat(problem.getStatus()).isEqualTo(404);
    assertThat(problem.getProperties()).containsEntry("code", "automation_not_found");
  }

  @Test
  @DisplayName("the quota is a 409 with its own code")
  void quota() {
    ProblemDetail problem = resolver.tooMany(new TooManyAutomationsException(3));
    assertThat(problem.getStatus()).isEqualTo(409);
    assertThat(problem.getProperties()).containsEntry("code", "too_many_automations");
  }

  @Test
  @DisplayName("a rejected value is a 400 carrying the reason")
  void invalid() {
    ProblemDetail problem = resolver.invalid(new IllegalArgumentException("bad"));
    assertThat(problem.getStatus()).isEqualTo(400);
    assertThat(problem.getDetail()).isEqualTo("bad");
    assertThat(problem.getProperties()).containsEntry("code", "invalid_request");
  }
}
