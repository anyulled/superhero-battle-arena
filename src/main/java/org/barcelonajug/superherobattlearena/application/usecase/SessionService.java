package org.barcelonajug.superherobattlearena.application.usecase;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.SessionRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionService {

  private static final Logger log = LoggerFactory.getLogger(SessionService.class);

  private final SessionRepositoryPort sessionRepository;

  public SessionService(SessionRepositoryPort sessionRepository) {
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

    log.info("Created new session: {}", savedSession.getSessionId());
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
}
