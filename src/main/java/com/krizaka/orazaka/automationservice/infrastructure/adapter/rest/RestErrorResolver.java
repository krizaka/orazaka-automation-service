package com.krizaka.orazaka.automationservice.infrastructure.adapter.rest;

import com.krizaka.orazaka.automationservice.domain.exception.AutomationNotFoundException;
import com.krizaka.orazaka.automationservice.domain.exception.TooManyAutomationsException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * One error shape for the API: RFC 9457 problem details with a stable {@code code} (ADR-083 §2).
 * Messages never echo a secret or a request body.
 */
@RestControllerAdvice
class RestErrorResolver {

  private static final String CODE = "code";

  @ExceptionHandler(AutomationNotFoundException.class)
  ProblemDetail notFound(AutomationNotFoundException e) {
    return problem(HttpStatus.NOT_FOUND, "automation_not_found", "No such automation.");
  }

  @ExceptionHandler(TooManyAutomationsException.class)
  ProblemDetail tooMany(TooManyAutomationsException e) {
    return problem(HttpStatus.CONFLICT, "too_many_automations", e.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  ProblemDetail invalid(IllegalArgumentException e) {
    return problem(HttpStatus.BAD_REQUEST, "invalid_request", e.getMessage());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ProblemDetail unreadable(HttpMessageNotReadableException e) {
    return problem(HttpStatus.BAD_REQUEST, "invalid_request", "The body is not valid JSON.");
  }

  private static ProblemDetail problem(HttpStatus status, String code, String detail) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setType(URI.create("https://orazaka.com/problems/" + code));
    problem.setProperty(CODE, code);
    return problem;
  }
}
