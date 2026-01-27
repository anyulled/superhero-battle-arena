package org.barcelonajug.superherobattlearena.adapter.in.web;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.SessionRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Session;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionRepositoryPort sessionRepository;

    public SessionController(SessionRepositoryPort sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @PostMapping
    public ResponseEntity<UUID> createSession() {
        Session session = new Session(UUID.randomUUID(), OffsetDateTime.now(), true);
        sessionRepository.save(session);
        return ResponseEntity.ok(session.getSessionId());
    }
}
