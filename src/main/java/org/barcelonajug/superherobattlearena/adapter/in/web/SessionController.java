package org.barcelonajug.superherobattlearena.adapter.in.web;

import org.barcelonajug.superherobattlearena.application.usecase.SessionUseCase;
import org.barcelonajug.superherobattlearena.domain.Session;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

  private final SessionUseCase sessionUseCase;

  public SessionController(SessionUseCase sessionUseCase) {
    this.sessionUseCase = sessionUseCase;
  }

  @PostMapping
  public ResponseEntity<Session> createSession() {
    return ResponseEntity.ok(sessionUseCase.createSession());
  }

  @GetMapping("/active")
  public ResponseEntity<Session> getActiveSession() {
    return sessionUseCase
        .getActiveSession()
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
