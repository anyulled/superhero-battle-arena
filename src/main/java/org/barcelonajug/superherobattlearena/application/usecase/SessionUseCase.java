package org.barcelonajug.superherobattlearena.application.usecase;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.SessionRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionUseCase {

  private static final Logger log = LoggerFactory.getLogger(SessionUseCase.class);

  private final SessionRepositoryPort sessionRepository;

  public SessionUseCase(SessionRepositoryPort sessionRepository) {
    this.sessionRepository = sessionRepository;
  }

  @Transactional
  public Session createSession() {
    log.info("Creating new session");

    // Deactivate current active session if exists
    sessionRepository
        .findByActiveTrue()
        .ifPresent(
            session -> {
              log.info("Deactivating previous session: {}", session.getSessionId());
              session.setActive(false);
              sessionRepository.save(session);
            });

    Session newSession = new Session(UUID.randomUUID(), OffsetDateTime.now(), true);
    Session savedSession = sessionRepository.save(newSession);

    MDC.put("sessionId", savedSession.getSessionId().toString());
    log.info("Created new session: {}", savedSession.getSessionId());
    MDC.remove("sessionId");

    return savedSession;
  }

  @Transactional(readOnly = true)
  public Optional<Session> getActiveSession() {
    Optional<Session> activeSession = sessionRepository.findByActiveTrue();
    activeSession.ifPresentOrElse(
        session -> log.debug("Found active session: {}", session.getSessionId()),
        () -> log.debug("No active session found"));
    return activeSession;
  }

  @Transactional(readOnly = true)
  public java.util.List<Session> listSessions() {
    return sessionRepository.findAll();
  }

  @Transactional
  public Session startSession(UUID sessionId) {
    UUID id = (sessionId != null) ? sessionId : UUID.randomUUID();
    MDC.put("sessionId", id.toString());

    try {
      log.info("Starting session - sessionId={}", id);

      // Deactivate all existing active sessions to recover from multiple-active bug
      sessionRepository
          .findAll()
          .stream()
          .filter(Session::isActive)
          .forEach(
              existingSession -> {
                log.info("Deactivating previous session: {}", existingSession.getSessionId());
                existingSession.setActive(false);
                sessionRepository.save(existingSession);
              });

      Session session = new Session(id, OffsetDateTime.now(), true);
      Session savedSession = sessionRepository.save(session);
      log.info("Session started successfully - sessionId={}", id);
      return savedSession;
    } finally {
      MDC.remove("sessionId");
    }
  }

  @Transactional(readOnly = true)
  public Optional<Session> getSession(UUID sessionId) {
    return sessionRepository.findById(sessionId);
  }
}
