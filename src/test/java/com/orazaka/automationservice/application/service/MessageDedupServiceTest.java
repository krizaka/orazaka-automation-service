package com.orazaka.automationservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

/** Both halves of the invariant: seen twice, processed once — and failed, seen again (ADR-058). */
class MessageDedupServiceTest {

  private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
  private final MessageDedupService service = new MessageDedupService(jdbcTemplate);

  @Test
  @DisplayName("the first caller claims it")
  void firstCallerClaims() {
    assertThat(service.claim("automation.jobs", "m-1")).isTrue();
  }

  @Test
  @DisplayName("the second is refused by the constraint, not by a prior read")
  void secondCallerIsRefused() {
    // This class used to SELECT EXISTS and then INSERT. Two deliveries of one message both read
    // "not present" and both proceeded — under the exact condition dedup exists for, because
    // at-least-once redelivery overlaps a slow first attempt rather than following it.
    when(jdbcTemplate.update(anyString(), anyString(), anyString()))
        .thenThrow(new DataIntegrityViolationException("duplicate key"));

    assertThat(service.claim("automation.jobs", "m-1")).isFalse();
  }

  @Test
  @DisplayName("no messageId means no deduplication was asked for, so it is claimable")
  void aMissingIdIsClaimable() {
    assertThat(service.claim("automation.jobs", null)).isTrue();
    assertThat(service.claim("automation.jobs", "  ")).isTrue();
    verify(jdbcTemplate, never()).update(anyString(), anyString(), anyString());
  }

  @Test
  @DisplayName("a released claim can be taken again — a failed message is not lost")
  void releaseDeletesTheClaim() {
    service.release("automation.jobs", "m-1");

    verify(jdbcTemplate)
        .update(
            "DELETE FROM processed_messages WHERE consumer = ? AND message_id = ?",
            "automation.jobs",
            "m-1");
  }

  @Test
  @DisplayName("releasing nothing touches nothing")
  void releasingABlankIdIsANoOp() {
    service.release("automation.jobs", null);

    verify(jdbcTemplate, never()).update(anyString(), anyString(), anyString());
  }
}
