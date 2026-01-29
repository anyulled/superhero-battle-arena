package org.barcelonajug.superherobattlearena.application.usecase;

import org.barcelonajug.superherobattlearena.application.port.out.SessionRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {

    private final SessionRepositoryPort sessionRepository;

    public SessionService(SessionRepositoryPort sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public Session createSession() {
        // Deactivate current active session if exists
        sessionRepository.findByActiveTrue().ifPresent(session -> {
            session.setActive(false);
            sessionRepository.save(session);
        });

        Session newSession = new Session(UUID.randomUUID(), OffsetDateTime.now(), true);
        return sessionRepository.save(newSession);
    }

    @Transactional(readOnly = true)
    public Optional<Session> getActiveSession() {
        return sessionRepository.findByActiveTrue();
    }
}
