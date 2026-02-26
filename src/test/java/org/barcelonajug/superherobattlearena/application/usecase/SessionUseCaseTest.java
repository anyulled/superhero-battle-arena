package org.barcelonajug.superherobattlearena.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.barcelonajug.superherobattlearena.application.port.out.SessionRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SessionUseCaseTest {

    private SessionRepositoryPort sessionRepository;
    private SessionUseCase sessionUseCase;

    @BeforeEach
    void setUp() {
        sessionRepository = mock(SessionRepositoryPort.class);
        sessionUseCase = new SessionUseCase(sessionRepository);
    }

    @Test
    void createSession_shouldDeactivatePreviousAndSaveNew() {
        Session previous = new Session(UUID.randomUUID(), OffsetDateTime.now(ZoneOffset.UTC), true);
        when(sessionRepository.findByActiveTrue()).thenReturn(Optional.of(previous));
        when(sessionRepository.save(any(Session.class))).thenAnswer(i -> i.getArgument(0));

        Session result = sessionUseCase.createSession();

        assertThat(previous.isActive()).isFalse();
        verify(sessionRepository).save(previous);
        verify(sessionRepository).save(result);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void createSession_shouldWorkWhenNoPreviousActive() {
        when(sessionRepository.findByActiveTrue()).thenReturn(Optional.empty());
        when(sessionRepository.save(any(Session.class))).thenAnswer(i -> i.getArgument(0));

        Session result = sessionUseCase.createSession();

        verify(sessionRepository, times(1)).save(any(Session.class));
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void getActiveSession_shouldReturnFromRepository() {
        Session active = new Session(UUID.randomUUID(), OffsetDateTime.now(ZoneOffset.UTC), true);
        when(sessionRepository.findByActiveTrue()).thenReturn(Optional.of(active));

        Optional<Session> result = sessionUseCase.getActiveSession();

        assertThat(result).isPresent().contains(active);
    }

    @Test
    void getActiveSession_shouldLogWhenEmpty() {
        when(sessionRepository.findByActiveTrue()).thenReturn(Optional.empty());
        Optional<Session> result = sessionUseCase.getActiveSession();
        assertThat(result).isEmpty();
    }

    @Test
    void listSessions_shouldReturnAllFromRepository() {
        sessionUseCase.listSessions();
        verify(sessionRepository).findAll();
    }

    @Test
    void startSession_shouldDeactivateAllActiveAndSaveNew() {
        UUID newId = UUID.randomUUID();
        Session s1 = new Session(UUID.randomUUID(), OffsetDateTime.now(ZoneOffset.UTC), true);
        Session s2 = new Session(UUID.randomUUID(), OffsetDateTime.now(ZoneOffset.UTC), false);
        when(sessionRepository.findAll()).thenReturn(List.of(s1, s2));
        when(sessionRepository.save(any(Session.class))).thenAnswer(i -> i.getArgument(0));

        Session result = sessionUseCase.startSession(newId);

        assertThat(s1.isActive()).isFalse();
        verify(sessionRepository).save(s1);
        assertThat(result.getSessionId()).isEqualTo(newId);
        assertThat(result.isActive()).isTrue();
        verify(sessionRepository).save(result);
    }

    @Test
    @SuppressWarnings("NullAway")
    void startSession_shouldGenerateIdWhenNull() {
        when(sessionRepository.findAll()).thenReturn(List.of());
        when(sessionRepository.save(any(Session.class))).thenAnswer(i -> i.getArgument(0));

        Session result = sessionUseCase.startSession((UUID) null);

        assertThat(result.getSessionId()).isNotNull();
        verify(sessionRepository).save(result);
    }

    @Test
    void getSession_shouldReturnFromRepository() {
        UUID id = UUID.randomUUID();
        sessionUseCase.getSession(id);
        verify(sessionRepository).findById(id);
    }
}
